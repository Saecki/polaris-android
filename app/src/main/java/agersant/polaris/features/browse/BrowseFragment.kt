package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.R
import agersant.polaris.databinding.FragmentBrowseBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

class BrowseFragment : Fragment() {

    internal enum class NavigationMode {
        PATH,
        RANDOM,
        RECENT,
    }

    private enum class DisplayMode {
        EXPLORER,
        DISCOGRAPHY,
        ALBUM,
    }

    companion object {
        const val PATH = "PATH"
        const val NAVIGATION_MODE = "NAVIGATION_MODE"
    }

    private val model: BrowseViewModel by viewModels()
    private lateinit var contentHolder: FrameLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var errorMessage: View
    private lateinit var errorRetry: Button
    private lateinit var toolbar: Toolbar
    private lateinit var navigationMode: NavigationMode
    private var content: BrowseContent? = null
    private lateinit var onRefresh: BrowseContent.OnRefreshListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)

        val binding = FragmentBrowseBinding.inflate(inflater)
        contentHolder = binding.contentHolder
        progressBar = binding.progressBar
        errorMessage = binding.errorMessage
        errorRetry = binding.errorRetry
        toolbar = requireActivity().findViewById(R.id.toolbar)

        model.items.observe(viewLifecycleOwner, this::displayContent)
        model.fetching.observe(viewLifecycleOwner, progressBar::isVisible::set)
        model.error.observe(viewLifecycleOwner, errorMessage::isVisible::set)

        errorRetry.setOnClickListener { loadContent() }
        onRefresh = BrowseContent.OnRefreshListener {
            model.scrollPosition = 0
            loadContent()
        }

        navigationMode = requireArguments().getSerializable(NAVIGATION_MODE) as NavigationMode

        content = null
        if (model.initialCreation) {
            loadContent()
            model.initialCreation = false
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        model.scrollPosition = content?.saveScrollPosition() ?: 0
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
                val path = arguments?.getString(PATH).orEmpty()
                model.loadPath(path)
            }
            NavigationMode.RANDOM -> model.loadRandom()
            NavigationMode.RECENT -> model.loadRecent()
        }
    }

    private fun getDisplayModeForItems(items: List<CollectionItem>): DisplayMode {
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
        } else {
            DisplayMode.EXPLORER
        }
    }

    private fun displayContent(items: List<CollectionItem>) = content.let {

        val replaceContent = { c: BrowseContent ->
            contentHolder.removeAllViews()
            contentHolder.addView(c.root)
            c.setOnRefreshListener(onRefresh)
            content = c
            c
        }

        val browseContent = when (getDisplayModeForItems(items)) {
            DisplayMode.EXPLORER -> it as? BrowseContentExplorer ?: run {
                replaceContent(BrowseContentExplorer(requireContext(), model.api, model.playbackQueue))
            }
            DisplayMode.ALBUM -> it as? BrowseContentAlbum ?: run {
                replaceContent(BrowseContentAlbum(requireContext(), model.api, model.playbackQueue))
            }
            DisplayMode.DISCOGRAPHY -> it as? BrowseContentDiscography ?: run {
                val sortAlbums = navigationMode == NavigationMode.PATH
                replaceContent(BrowseContentDiscography(requireContext(), model.api, model.playbackQueue, sortAlbums))
            }
        }

        browseContent.updateItems(items)
        browseContent.restoreScrollPosition(model.scrollPosition)
    }
}
