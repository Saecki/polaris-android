package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.PlaybackQueue
import agersant.polaris.api.API
import agersant.polaris.databinding.ViewBrowseExplorerBinding
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout

internal class BrowseContentExplorer(
    context: Context,
    api: API,
    playbackQueue: PlaybackQueue,
) : BrowseContent(context) {

    override val root: View

    private val swipeRefresh: SwipyRefreshLayout
    private val recyclerView: RecyclerView
    private val adapter: BrowseAdapter

    init {
        val inflater = LayoutInflater.from(context)
        val binding = ViewBrowseExplorerBinding.inflate(inflater)
        root = binding.root
        swipeRefresh = binding.swipeRefresh
        recyclerView = binding.recyclerView

        root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        recyclerView.setHasFixedSize(true)

        val callback = BrowseTouchCallback()
        callback.setOnEnableRefresh(swipeRefresh::setEnabled)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        adapter = BrowseAdapterExplorer(api, playbackQueue)
        recyclerView.adapter = adapter
    }

    override fun updateItems(items: List<CollectionItem>) {
        swipeRefresh.isRefreshing = false
        val sortedItems = items.sortedWith { a, b -> a.name.compareTo(b.name, ignoreCase = true) }
        adapter.updateItems(sortedItems)
    }

    override fun saveScrollPosition(): Int {
        val layoutManger = recyclerView.layoutManager as LinearLayoutManager
        return layoutManger.findFirstCompletelyVisibleItemPosition()
    }

    override fun restoreScrollPosition(position: Int) {
        recyclerView.scrollToPosition(position)
    }

    override fun setOnRefreshListener(listener: OnRefreshListener) {
        swipeRefresh.setOnRefreshListener { listener.onRefresh() }
    }
}
