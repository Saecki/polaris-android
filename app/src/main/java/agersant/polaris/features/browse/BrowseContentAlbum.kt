package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.PlaybackQueue
import agersant.polaris.R
import agersant.polaris.api.API
import agersant.polaris.databinding.ViewBrowseAlbumBinding
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.postDelayed
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout

internal class BrowseContentAlbum(
    context: Context,
    private val api: API,
    playbackQueue: PlaybackQueue,
) : BrowseContent(context) {

    override val root: View
    private val swipeRefresh: SwipyRefreshLayout
    private val recyclerView: RecyclerView
    private val artwork: ImageView
    private val album: TextView
    private val artist: TextView
    private val queueAll: ExtendedFloatingActionButton
    private val divider: View
    private val adapter: BrowseAdapter

    init {
        val inflater = LayoutInflater.from(context)
        val binding = ViewBrowseAlbumBinding.inflate(inflater)

        root = binding.root
        swipeRefresh = binding.swipeRefresh
        recyclerView = binding.recyclerView
        artwork = binding.albumArtwork
        album = binding.album
        artist = binding.artist
        queueAll = binding.queueAll
        divider = binding.headerDivider

        root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        recyclerView.setHasFixedSize(true)

        val callback = BrowseTouchCallback()
        callback.setOnEnableRefresh(swipeRefresh::setEnabled)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        adapter = BrowseAdapterAlbum(api, playbackQueue)
        recyclerView.adapter = adapter

        recyclerView.setOnScrollChangeListener { _, _, _, _, oldScrollY ->
            updateDividerVisibility()

            if (oldScrollY < 0) {
                queueAll.shrink()
            } else if (oldScrollY > 0) {
                queueAll.extend()
            }
        }

        queueAll.setOnClickListener {
            playbackQueue.addItems(adapter.items)
            queueAll.setIconResource(R.drawable.ic_check_24)
            Handler(context.mainLooper).postDelayed(1000) {
                queueAll.setIconResource(R.drawable.ic_playlist_play_24)
            }
        }
    }

    override fun updateItems(items: List<CollectionItem>) {
        swipeRefresh.isRefreshing = false

        val sortedItems = items.sortedWith { a, b ->
            val discDifference = a.discNumber - b.discNumber
            if (discDifference != 0) {
                discDifference
            } else {
                a.trackNumber - b.trackNumber
            }
        }
        adapter.updateItems(sortedItems)
        val item = sortedItems.first()

        var artistString = item.albumArtist ?: item.artist.orEmpty()
        if (item.year != -1) {
            artistString += " • ${item.year}"
        }
        artist.text = artistString
        album.text = item.album.orEmpty()
        if (item.artwork != null) {
            api.loadImageIntoView(item, artwork)
        } else {
            artwork.setImageResource(R.drawable.ic_fallback_artwork)
        }
    }

    override fun saveScrollPosition(): Int {
        val layoutManger = recyclerView.layoutManager as LinearLayoutManager
        return layoutManger.findFirstVisibleItemPosition()
    }

    override fun restoreScrollPosition(position: Int) {
        if (position >= 1) {
            recyclerView.scrollToPosition(position)
            queueAll.shrink()
            Handler(context.mainLooper).post {
                updateDividerVisibility()
            }
        }
    }

    override fun setOnRefreshListener(listener: OnRefreshListener) {
        swipeRefresh.setOnRefreshListener { listener.onRefresh() }
    }

    private fun updateDividerVisibility() {
        if (recyclerView.canScrollVertically(-1)) {
            divider.visibility = View.VISIBLE
        } else {
            divider.visibility = View.INVISIBLE
        }
    }
}
