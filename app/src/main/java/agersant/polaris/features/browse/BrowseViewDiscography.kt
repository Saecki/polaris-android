package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.PlaybackQueue
import agersant.polaris.api.API
import agersant.polaris.databinding.ViewBrowseDiscographyBinding
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout

@SuppressLint("ViewConstructor")
internal class BrowseViewDiscography(
    context: Context,
    api: API,
    playbackQueue: PlaybackQueue,
    private val sortAlbums: Boolean = false,
) : BrowseViewContent(context) {

    private val recyclerView: RecyclerView
    private val adapter: BrowseAdapter
    private val swipeRefresh: SwipyRefreshLayout

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ViewBrowseDiscographyBinding.inflate(inflater, this, true)
        swipeRefresh = binding.swipeRefresh

        recyclerView = binding.browseRecyclerView
        recyclerView.setHasFixedSize(true)

        val callback: ItemTouchHelper.Callback = BrowseTouchCallback()
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        adapter = BrowseAdapterDiscography(api, playbackQueue)
        recyclerView.adapter = adapter
    }

    override fun updateItems(items: List<CollectionItem>) {
        val sortedItems = if (sortAlbums) items.sortedBy { it.year } else items
        adapter.updateItems(sortedItems)
    }

    override fun setOnRefreshListener(listener: SwipyRefreshLayout.OnRefreshListener?) {
        swipeRefresh.isEnabled = listener != null
        swipeRefresh.setOnRefreshListener(listener)
    }

    override fun getScrollPosition(): Int {
        val layoutManger = recyclerView.layoutManager as LinearLayoutManager
        return layoutManger.findFirstCompletelyVisibleItemPosition()
    }

    override fun setScrollPosition(position: Int) {
        recyclerView.scrollToPosition(position)
    }
}
