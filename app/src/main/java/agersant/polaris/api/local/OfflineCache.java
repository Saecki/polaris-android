package agersant.polaris.api.local;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import agersant.polaris.CollectionItem;
import agersant.polaris.PlaybackQueue;
import agersant.polaris.PolarisApplication;
import agersant.polaris.PolarisPlayer;
import agersant.polaris.R;
import agersant.polaris.api.ThumbnailSize;


public class OfflineCache {

    public static final String AUDIO_CACHED = "AUDIO_CACHED";
    public static final String AUDIO_REMOVED_FROM_CACHE = "AUDIO_REMOVED_FROM_CACHE";
    private static final String ITEM_FILENAME = "__polaris__item";
    private static final String AUDIO_FILENAME = "__polaris__audio";
    private static final String ARTWORK_SMALL_FILENAME = "__polaris__artwork_small";
    private static final String ARTWORK_LARGE_FILENAME = "__polaris__artwork_large";
    private static final String ARTWORK_NATIVE_FILENAME = "__polaris__artwork_native";
    private static final String META_FILENAME = "__polaris__meta";
    private static final int FIRST_VERSION = 1;
    private static final int VERSION = 4;
    private static final int BUFFER_SIZE = 1024 * 64;
    private final SharedPreferences preferences;
    private final String cacheSizeKey;
    private final PlaybackQueue playbackQueue;
    private final PolarisPlayer player;
    private final DataSource.Factory dataSourceFactory;
    private File root;

    public OfflineCache(Context context, PlaybackQueue playbackQueue, PolarisPlayer player) {

        Resources resources = context.getResources();

        dataSourceFactory = new DefaultDataSourceFactory(context, "Polaris Local");
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        cacheSizeKey = resources.getString(R.string.pref_key_offline_cache_size);
        this.playbackQueue = playbackQueue;
        this.player = player;

        for (int i = FIRST_VERSION; i <= VERSION; i++) {
            root = new File(context.getExternalCacheDir(), "collection");
            root = new File(root, "v" + i);
            if (i != VERSION) {
                deleteDirectory(root);
            }
        }
    }

    private void write(CollectionItem item, OutputStream storage) throws IOException {
        ObjectOutputStream objOut = new ObjectOutputStream(storage);
        objOut.writeObject(item);
        objOut.close();
    }

    private void write(FileInputStream audio, OutputStream storage) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while ((read = audio.read(buffer)) > 0) {
            storage.write(buffer, 0, read);
        }
    }

    private void write(Bitmap image, OutputStream storage) {
        image.compress(Bitmap.CompressFormat.PNG, 100, storage);
    }

    private void write(ItemCacheMetadata metadata, OutputStream storage) throws IOException {
        ObjectOutputStream objOut = new ObjectOutputStream(storage);
        objOut.writeObject(metadata);
        objOut.close();
    }

    private void listDeletionCandidates(File path, ArrayList<DeletionCandidate> candidates) {
        File[] files = path.listFiles();
        for (File child : files) {
            File audio = new File(child, AUDIO_FILENAME);
            if (audio.exists()) {

                ItemCacheMetadata metadata = new ItemCacheMetadata();
                metadata.lastUse = new Date(0L);

                File meta = new File(child, META_FILENAME);
                if (meta.exists()) {
                    try {
                        metadata = readMetadata(meta);
                    } catch (IOException e) {
                        System.out.println("Error reading file metadata for " + child + " " + e);
                    }
                }

                CollectionItem item = null;
                try {
                    item = readItem(child);
                } catch (Exception e) {
                    System.out.println("Error reading collection item for " + child + " " + e);
                }

                DeletionCandidate candidate = new DeletionCandidate(child, metadata, item);
                candidates.add(candidate);
            } else if (child.isDirectory()) {
                listDeletionCandidates(child, candidates);
            }
        }
    }

    private long getCacheSize(File file) {
        long size = 0;
        if (!file.exists()) {
            return 0;
        }
        File[] files = file.listFiles();
        if (files != null) {
            for (File child : files) {
                size += child.length();
                if (child.isDirectory()) {
                    size += getCacheSize(child);
                }
            }
        }
        return size;
    }

    private long getCacheCapacity() {
        String cacheSizeString = preferences.getString(cacheSizeKey, "0");
        long cacheSize = Long.parseLong(cacheSizeString);
        if (cacheSize < 0) {
            return Long.MAX_VALUE;
        }
        return cacheSize * 1024 * 1024;
    }

    private boolean removeOldAudio(File path, CollectionItem newItem, long bytesToSave) {
        ArrayList<DeletionCandidate> candidates = new ArrayList<>();
        listDeletionCandidates(path, candidates);

        Collections.sort(candidates, (a, b) -> {
            if (a.item == null && b.item != null) {
                return -1;
            }
            if (b.item == null && a.item != null) {
                return 1;
            }
            //noinspection ConstantConditions
            if (b.item != null && a.item != null) {
                return -playbackQueue.comparePriorities(player.getCurrentItem(), a.item, b.item);
            }
            return (int) (a.metadata.lastUse.getTime() - b.metadata.lastUse.getTime());
        });

        long cleared = 0;
        for (DeletionCandidate candidate : candidates) {
            if (candidate.item != null) {
                if (playbackQueue.comparePriorities(player.getCurrentItem(), candidate.item, newItem) <= 0) {
                    continue;
                }
            }

            File audio = new File(candidate.cachePath, AUDIO_FILENAME);
            if (audio.exists()) {
                long size = audio.length();
                if (audio.delete()) {
                    System.out.println("Deleting " + audio);
                    cleared += size;
                }
                if (cleared >= bytesToSave) {
                    break;
                }
            }
        }

        if (cleared > 0) {
            broadcast(AUDIO_REMOVED_FROM_CACHE);
        }
        return cleared >= bytesToSave;
    }

    public synchronized boolean makeSpace(CollectionItem item) {
        long cacheSize = getCacheSize(root);
        long cacheCapacity = getCacheCapacity();
        long overflow = cacheSize - cacheCapacity;
        boolean success = true;
        if (overflow > 0) {
            success = removeOldAudio(root, item, overflow);
            removeEmptyDirectories(root);
        }
        return success;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteDirectory(File path) {
        if (!path.exists()) {
            return;
        }
        File[] files = path.listFiles();
        if (files == null) {
            return;
        }
        for (File child : files) {
            if (child.isDirectory()) {
                deleteDirectory(child);
            } else {
                child.delete();
            }
        }
        path.delete();
    }

    private void removeEmptyDirectories(File path) {
        // TODO: Catastrophic complexity
        File[] files = path.listFiles();
        for (File child : files) {
            if (child.isDirectory()) {
                if (!containsAudio(child)) {
                    System.out.println("Deleting " + child);
                    deleteDirectory(child);
                } else {
                    removeEmptyDirectories(child);
                }
            }
        }
    }

    public synchronized void putAudio(CollectionItem item, FileInputStream audio) {

        makeSpace(item); // TODO we don't need this called so often. Every n minutes should do.

        String path = item.getPath();

        try (FileOutputStream itemOut = new FileOutputStream(createCacheFile(path, CacheDataType.ITEM))) {
            write(item, itemOut);
        } catch (IOException e) {
            System.out.println("Error while caching item for local use: " + e);
            return;
        }

        if (audio != null) {
            try (FileOutputStream itemOut = new FileOutputStream(createCacheFile(path, CacheDataType.AUDIO))) {
                write(audio, itemOut);
                broadcast(AUDIO_CACHED);
            } catch (IOException e) {
                System.out.println("Error while caching audio for local use: " + e);
                return;
            }
        }

        if (!hasMetadata(path)) {
            saveMetadata(path, new ItemCacheMetadata());
        }

        System.out.println("Saved audio to offline cache: " + path);
    }

    public synchronized void putImage(CollectionItem item, ThumbnailSize size, Bitmap image) {
        String path = item.getPath();

        try (FileOutputStream itemOut = new FileOutputStream(createCacheFile(path, CacheDataType.ITEM))) {
            write(item, itemOut);
        } catch (IOException e) {
            System.out.println("Error while caching item for local use: " + e);
        }

        if (image != null) {
            String artworkPath = item.getArtwork();
            CacheDataType cacheDataType = getImageCacheDataType(size);
            try (FileOutputStream itemOut = new FileOutputStream(createCacheFile(artworkPath, cacheDataType))) {
                write(image, itemOut);
            } catch (IOException e) {
                System.out.println("Error while caching artwork for local use: " + e);
            }
        }

        System.out.println("Saved image to offline cache: " + path);
    }

    private File getCacheDir(String virtualPath) {
        String path = virtualPath.replace("\\", File.separator);
        return new File(root, path);
    }

    private File getCacheFile(String virtualPath, CacheDataType type) {
        File file = getCacheDir(virtualPath);
        switch (type) {
            case ITEM:
                return new File(file, ITEM_FILENAME);
            case AUDIO:
                return new File(file, AUDIO_FILENAME);
            case ARTWORK_SMALL:
                return new File(file, ARTWORK_SMALL_FILENAME);
            case ARTWORK_LARGE:
                return new File(file, ARTWORK_LARGE_FILENAME);
            case ARTWORK_NATIVE:
                return new File(file, ARTWORK_NATIVE_FILENAME);
            case META:
            default:
                return new File(file, META_FILENAME);
        }
    }

    private File createCacheFile(String virtualPath, CacheDataType type) throws IOException {
        File file = getCacheFile(virtualPath, type);

        File parent = file.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("Could not create cache directory: " + parent);
            }
        }
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Could not create cache file: " + file);
            }
        }

        return file;
    }

    public boolean hasAudio(String path) {
        File file = getCacheFile(path, CacheDataType.AUDIO);
        return file.exists();
    }

    boolean hasImage(String virtualPath, ThumbnailSize size) {
        CacheDataType cacheDataType = getImageCacheDataType(size);
        File file = getCacheFile(virtualPath, cacheDataType);
        return file.exists();
    }

    MediaSource getAudio(String virtualPath) throws IOException {
        if (!hasAudio(virtualPath)) {
            throw new FileNotFoundException();
        }
        if (hasMetadata(virtualPath)) {
            ItemCacheMetadata metadata = getMetadata(virtualPath);
            metadata.lastUse = new Date();
            saveMetadata(virtualPath, metadata);
        }
        Uri uri = Uri.fromFile(getCacheFile(virtualPath, CacheDataType.AUDIO));
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
    }

    CacheDataType getImageCacheDataType(ThumbnailSize size) {
        switch (size) {
            case Large:
                return CacheDataType.ARTWORK_LARGE;
            case Native:
                return CacheDataType.ARTWORK_NATIVE;
            case Small:
            default:
                return CacheDataType.ARTWORK_SMALL;
        }
    }

    Bitmap getImage(String virtualPath, ThumbnailSize size) throws IOException {
        if (!hasImage(virtualPath, size)) {
            throw new FileNotFoundException();
        }
        CacheDataType cacheDataType = getImageCacheDataType(size);
        File file = getCacheFile(virtualPath, cacheDataType);
        FileInputStream fileInputStream = new FileInputStream(file);
        return BitmapFactory.decodeFileDescriptor(fileInputStream.getFD());
    }

    private void saveMetadata(String virtualPath, ItemCacheMetadata metadata) {
        try (FileOutputStream metaOut = new FileOutputStream(createCacheFile(virtualPath, CacheDataType.META))) {
            write(metadata, metaOut);
        } catch (IOException e) {
            System.out.println("Error while caching metadata for local use: " + e);
        }
    }

    private boolean hasMetadata(String virtualPath) {
        File file = getCacheFile(virtualPath, CacheDataType.META);
        return file.exists();
    }

    private ItemCacheMetadata readMetadata(File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)
        ) {
            return (ItemCacheMetadata) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new FileNotFoundException();
        }
    }

    private ItemCacheMetadata getMetadata(String virtualPath) throws IOException {
        if (!hasMetadata(virtualPath)) {
            throw new FileNotFoundException();
        }
        File file = getCacheFile(virtualPath, CacheDataType.META);
        return readMetadata(file);
    }

    public ArrayList<CollectionItem> browse(String path) {
        ArrayList<CollectionItem> out = new ArrayList<>();
        File dir = getCacheDir(path);
        File[] files = dir.listFiles();
        if (files == null) {
            return out;
        }

        for (File file : files) {
            try {
                if (!file.isDirectory()) {
                    continue;
                }
                if (isInternalFile(file)) {
                    continue;
                }
                CollectionItem item = readItem(file);
                if (item != null) {
                    if (item.isDirectory()) {
                        if (!containsAudio(file)) {
                            continue;
                        }
                    }
                    out.add(item);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error while reading offline cache: " + e);
            }
        }

        return out;
    }

    public ArrayList<CollectionItem> flatten(String path) {
        File dir = getCacheDir(path);
        return flattenDir(dir);
    }

    private ArrayList<CollectionItem> flattenDir(File source) {
        ArrayList<CollectionItem> out = new ArrayList<>();
        File[] files = source.listFiles();
        if (files == null) {
            return out;
        }

        for (File file : files) {
            try {
                if (isInternalFile(file)) {
                    continue;
                }
                CollectionItem item = readItem(file);
                if (item == null) {
                    continue;
                }
                if (item.isDirectory()) {
                    ArrayList<CollectionItem> content = flattenDir(file);
                    if (content != null) {
                        out.addAll(content);
                    }
                } else if (hasAudio(item.getPath())) {
                    out.add(item);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error while reading offline cache: " + e);
                return null;
            }
        }
        return out;
    }

    public ArrayList<CollectionItem> search(String query) {
        return searchDir(root, query.toLowerCase());
    }

    private ArrayList<CollectionItem> searchDir(File dir, String query) {
        ArrayList<CollectionItem> out = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null) {
            return out;
        }

        for (File file : files) {
            try {
                if (isInternalFile(file)) {
                    continue;
                }
                CollectionItem item = readItem(file);
                if (item == null) {
                    continue;
                }

                if (item.isDirectory()) {
                    if (file.getName().toLowerCase().contains(query)) {
                        out.add(item);
                        continue;
                    }

                    ArrayList<CollectionItem> content = searchDir(file, query);
                    if (content != null) {
                        out.addAll(content);
                    }
                } else if (hasAudio(item.getPath())) {
                    boolean titleMatches = item.getTitle() != null && item.getTitle().toLowerCase().contains(query);
                    boolean albumMatches = item.getAlbum() != null && item.getAlbum().toLowerCase().contains(query);
                    boolean artistMatches = item.getArtist() != null && item.getArtist().toLowerCase().contains(query);
                    boolean albumArtistMatches = item.getAlbumArtist() != null && item.getAlbumArtist().toLowerCase().contains(query);
                    if (titleMatches || albumMatches || artistMatches || albumArtistMatches) {
                        out.add(item);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                return null;
            }
        }
        return out;
    }

    private boolean isInternalFile(File file) {
        String name = file.getName();
        return name.equals(ITEM_FILENAME)
            || name.equals(AUDIO_FILENAME)
            || name.equals(ARTWORK_SMALL_FILENAME)
            || name.equals(ARTWORK_LARGE_FILENAME)
            || name.equals(ARTWORK_NATIVE_FILENAME)
            || name.equals(META_FILENAME);
    }


    private boolean containsAudio(File file) {
        if (!file.isDirectory()) {
            return file.getName().equals(AUDIO_FILENAME);
        }
        File[] files = file.listFiles();
        for (File child : files) {
            if (containsAudio(child)) {
                return true;
            }
        }
        return false;
    }

    private CollectionItem readItem(File dir) throws IOException, ClassNotFoundException {
        File itemFile = new File(dir, ITEM_FILENAME);
        if (!itemFile.exists()) {
            if (dir.isDirectory()) {
                String path = root.toURI().relativize(dir.toURI()).getPath();
                return CollectionItem.directory(path);
            } else {
                return null;
            }
        }
        try (FileInputStream fileInputStream = new FileInputStream(itemFile);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)
        ) {
            return (CollectionItem) objectInputStream.readObject();
        }
    }

    private void broadcast(String event) {
        PolarisApplication application = PolarisApplication.getInstance();
        Intent intent = new Intent();
        intent.setAction(event);
        application.sendBroadcast(intent);
    }

    private enum CacheDataType {
        ITEM,
        AUDIO,
        ARTWORK_SMALL,
        ARTWORK_LARGE,
        ARTWORK_NATIVE,
        META,
    }

    private static class DeletionCandidate {
        final File cachePath;
        final ItemCacheMetadata metadata;
        final CollectionItem item;

        DeletionCandidate(File cachePath, ItemCacheMetadata metadata, CollectionItem item) {
            this.cachePath = cachePath;
            this.metadata = metadata;
            this.item = item;
        }
    }

}
