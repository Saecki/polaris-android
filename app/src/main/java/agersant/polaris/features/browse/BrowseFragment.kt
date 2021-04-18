package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.R
import agersant.polaris.databinding.FragmentBrowseBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout.OnRefreshListener

class BrowseFragment : Fragment() {

    internal enum class NavigationMode {
        PATH, RANDOM, RECENT
    }

    private enum class DisplayMode {
        EXPLORER, DISCOGRAPHY, ALBUM
    }

    companion object {
        const val PATH = "PATH"
        const val NAVIGATION_MODE = "NAVIGATION_MODE"
    }

    private val model: BrowseViewModel by viewModels()
    private lateinit var progressBar: ProgressBar
    private lateinit var errorMessage: View
    private lateinit var errorRetry: View
    private lateinit var contentHolder: ViewGroup
    private lateinit var navigationMode: NavigationMode
    private lateinit var toolbar: Toolbar
    private var onRefresh: OnRefreshListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)

        val binding = FragmentBrowseBinding.inflate(inflater)
        errorMessage = binding.browseErrorMessage
        progressBar = binding.progressBar
        contentHolder = binding.browseContentHolder
        errorRetry = binding.browseErrorRetry
        toolbar = requireActivity().findViewById(R.id.toolbar)

        model.items.observe(viewLifecycleOwner, this::displayContent)
        model.fetching.observe(viewLifecycleOwner, progressBar::isVisible::set)
        model.error.observe(viewLifecycleOwner, errorMessage::isVisible::set)

        errorRetry.setOnClickListener { loadContent() }

        navigationMode = requireArguments().getSerializable(NAVIGATION_MODE) as NavigationMode
        if (navigationMode == NavigationMode.RANDOM) {
            onRefresh = OnRefreshListener { loadContent() }
        }

        if (model.initialCreation) {
            loadContent()
            model.initialCreation = false
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        when (navigationMode) {
            NavigationMode.PATH -> toolbar.setTitle(R.string.collection_browse_directories)
            NavigationMode.RANDOM -> toolbar.setTitle(R.string.collection_random_albums)
            NavigationMode.RECENT -> toolbar.setTitle(R.string.collection_recently_added)
        }
    }

    private fun loadContent() {
        when (navigationMode) {
            NavigationMode.PATH -> {
                val path = requireArguments().getString(PATH) ?: ""
                model.loadPath(path)
            }
            NavigationMode.RANDOM -> model.loadRandom()
            NavigationMode.RECENT -> model.loadRecent()
        }
    }

    private fun getDisplayModeForItems(items: ArrayList<out CollectionItem>): DisplayMode {
        if (items.isEmpty()) {
            return DisplayMode.EXPLORER
        }

        val album = items[0].album
        var allSongs = true
        var allDirectories = true
        var oneHasArtwork = false
        var allHaveAlbum = album != null
        var allSameAlbum = true
        for (item in items) {
            allSongs = allSongs and !item.isDirectory
            allDirectories = allDirectories and item.isDirectory
            oneHasArtwork = oneHasArtwork or (item.artwork != null)
            allHaveAlbum = allHaveAlbum and (item.album != null)
            allSameAlbum = allSameAlbum and (album != null && album == item.album)
        }
        if (allDirectories && oneHasArtwork && allHaveAlbum) {
            return DisplayMode.DISCOGRAPHY
        }
        return if (album != null && allSongs && allSameAlbum) {
            DisplayMode.ALBUM
        } else DisplayMode.EXPLORER
    }

    private fun displayContent(items: ArrayList<out CollectionItem>) {
        val contentView: BrowseViewContent = when (getDisplayModeForItems(items)) {
            DisplayMode.EXPLORER -> BrowseViewExplorer(requireContext(), model.api, model.playbackQueue)
            DisplayMode.ALBUM -> BrowseViewAlbum(requireContext(), model.api, model.playbackQueue)
            DisplayMode.DISCOGRAPHY -> {
                val sortAlbums = navigationMode == NavigationMode.PATH
                BrowseViewDiscography(requireContext(), model.api, model.playbackQueue, sortAlbums)
            }
        }
        contentView.setItems(items)
        contentView.setOnRefreshListener(onRefresh)

        contentHolder.removeAllViews()
        contentHolder.addView(contentView)
    }
}
