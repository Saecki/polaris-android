package agersant.polaris.api.local;

import android.graphics.Bitmap;

import com.google.android.exoplayer2.source.MediaSource;

import java.io.IOException;
import java.util.ArrayList;

import agersant.polaris.CollectionItem;
import agersant.polaris.api.IPolarisAPI;
import agersant.polaris.api.ItemsCallback;
import agersant.polaris.api.ThumbnailSize;


public class LocalAPI implements IPolarisAPI {

    private OfflineCache offlineCache;

    public LocalAPI() {
    }

    public void initialize(OfflineCache offlineCache) {
        this.offlineCache = offlineCache;
    }

    public boolean hasAudio(CollectionItem item) {
        String path = item.getPath();
        return offlineCache.hasAudio(path);
    }

    public MediaSource getAudio(CollectionItem item) throws IOException {
        String path = item.getPath();
        return offlineCache.getAudio(path);
    }

    public boolean hasImage(CollectionItem item, ThumbnailSize size) {
        String path = item.getArtwork();
        return offlineCache.hasImage(path, size);
    }

    public Bitmap getImage(CollectionItem item, ThumbnailSize size) {
        String artworkPath = item.getArtwork();
        try {
            return offlineCache.getImage(artworkPath, size);
        } catch (IOException e) {
            System.out.println("Error while retrieving image from local cache: " + artworkPath);
        }
        return null;
    }

    public void browse(String path, ItemsCallback handlers) {
        ArrayList<CollectionItem> items = offlineCache.browse(path);
        if (items == null) {
            handlers.onError();
        } else {
            handlers.onSuccess(items);
        }
    }

    public void flatten(String path, ItemsCallback handlers) {
        ArrayList<CollectionItem> items = offlineCache.flatten(path);
        if (items == null) {
            handlers.onError();
        } else {
            handlers.onSuccess(items);
        }
    }

    public void search(String query, ItemsCallback handlers) {
        ArrayList<CollectionItem> items = offlineCache.search(query);
        if (items == null) {
            handlers.onError();
        } else {
            handlers.onSuccess(items);
        }
    }
}
