package agersant.polaris.features.browse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;

import agersant.polaris.CollectionItem;
import agersant.polaris.PlaybackQueue;
import agersant.polaris.PolarisApplication;
import agersant.polaris.PolarisState;
import agersant.polaris.R;
import agersant.polaris.api.API;
import agersant.polaris.api.ItemsCallback;
import agersant.polaris.api.remote.ServerAPI;
import agersant.polaris.databinding.FragmentBrowseBinding;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;


public class BrowseFragment extends Fragment {

    public static final String PATH = "PATH";
    public static final String NAVIGATION_MODE = "NAVIGATION_MODE";
    private FragmentBrowseBinding binding;
    private ProgressBar progressBar;
    private View errorMessage;
    private ViewGroup contentHolder;
    private ItemsCallback fetchCallback;
    private NavigationMode navigationMode;
    private SwipyRefreshLayout.OnRefreshListener onRefresh;
    private Toolbar toolbar;
    private ArrayList<? extends CollectionItem> items;
    private API api;
    private ServerAPI serverAPI;
    private PlaybackQueue playbackQueue;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        PolarisState state = PolarisApplication.getState();
        api = state.api;
        serverAPI = state.serverAPI;
        playbackQueue = state.playbackQueue;

        binding = FragmentBrowseBinding.inflate(inflater);
        errorMessage = binding.browseErrorMessage;
        progressBar = binding.progressBar;
        contentHolder = binding.browseContentHolder;
        toolbar = getActivity().findViewById(R.id.toolbar);

        binding.browseErrorRetry.setOnClickListener((view) -> loadContent());

        final BrowseFragment that = this;
        fetchCallback = new ItemsCallback() {
            @Override
            public void onSuccess(final ArrayList<? extends CollectionItem> items) {
                requireActivity().runOnUiThread(() -> {
                    that.progressBar.setVisibility(View.GONE);
                    that.items = items;
                    that.displayContent();
                });
            }

            @Override
            public void onError() {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    errorMessage.setVisibility(View.VISIBLE);
                });
            }
        };

        navigationMode = (NavigationMode) requireArguments().getSerializable(NAVIGATION_MODE);

        if (navigationMode == NavigationMode.RANDOM) {
            onRefresh = (SwipyRefreshLayoutDirection direction) -> loadContent();
        }

        loadContent();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        switch (navigationMode) {
            case PATH: {
                toolbar.setTitle(R.string.collection_browse_directories);
                break;
            }
            case RANDOM: {
                toolbar.setTitle(R.string.collection_random_albums);
                break;
            }
            case RECENT: {
                toolbar.setTitle(R.string.collection_recently_added);
                break;
            }
        }
    }

    private void loadContent() {
        progressBar.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.GONE);
        switch (navigationMode) {
            case PATH: {
                String path = requireArguments().getString(BrowseFragment.PATH);
                if (path == null) {
                    path = "";
                }
                loadPath(path);
                break;
            }
            case RANDOM: {
                loadRandom();
                break;
            }
            case RECENT: {
                loadRecent();
                break;
            }
        }
    }

    private void loadPath(String path) {
        api.browse(path, fetchCallback);
    }

    private void loadRandom() {
        serverAPI.getRandomAlbums(fetchCallback);
    }

    private void loadRecent() {
        serverAPI.getRecentAlbums(fetchCallback);
    }

    private DisplayMode getDisplayModeForItems(ArrayList<? extends CollectionItem> items) {
        if (items.isEmpty()) {
            return DisplayMode.EXPLORER;
        }

        String album = items.get(0).getAlbum();
        boolean allSongs = true;
        boolean allDirectories = true;
        boolean allHaveAlbum = album != null;
        boolean allSameAlbum = true;
        for (CollectionItem item : items) {
            allSongs &= !item.isDirectory();
            allDirectories &= item.isDirectory();
            allHaveAlbum &= item.getAlbum() != null;
            allSameAlbum &= album != null && album.equals(item.getAlbum());
        }

        if (allDirectories && allHaveAlbum) {
            return DisplayMode.DISCOGRAPHY;
        }

        if (album != null && allSongs && allSameAlbum) {
            return DisplayMode.ALBUM;
        }

        return DisplayMode.EXPLORER;
    }

    private enum DisplayMode {
        EXPLORER,
        DISCOGRAPHY,
        ALBUM,
    }

    enum NavigationMode {
        PATH,
        RANDOM,
        RECENT,
    }

    private void displayContent() {
        if (items == null) {
            return;
        }

        BrowseViewContent contentView = null;
        switch (getDisplayModeForItems(items)) {
            case EXPLORER:
                contentView = new BrowseViewExplorer(requireContext(), api, playbackQueue);
                break;
            case ALBUM:
                contentView = new BrowseViewAlbum(requireContext(), api, playbackQueue);
                break;
            case DISCOGRAPHY:
                contentView = new BrowseViewDiscography(requireContext(), api, playbackQueue);
                break;
        }

        contentView.setItems(items);
        contentView.setOnRefreshListener(onRefresh);

        contentHolder.removeAllViews();
        contentHolder.addView(contentView);
    }
}
