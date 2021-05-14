package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.PlaybackQueue
import agersant.polaris.api.API
import agersant.polaris.databinding.ViewBrowseExplorerBinding
import android.content.Context
import android.view.LayoutInflater
import android.view.View
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

    private val recyclerView: RecyclerView
    private val swipeRefresh: SwipyRefreshLayout
    private val adapter: BrowseAdapter

    init {
        val inflater = LayoutInflater.from(context)
        val binding = ViewBrowseExplorerBinding.inflate(inflater)
        recyclerView = binding.recyclerView
        swipeRefresh = binding.swipeRefresh

        root = binding.root
        recyclerView.setHasFixedSize(true)

        val callback: ItemTouchHelper.Callback = object : BrowseTouchCallback() {
            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                swipeRefresh.isEnabled = (actionState != ItemTouchHelper.ACTION_STATE_SWIPE)
            }
        }
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        adapter = BrowseAdapterExplorer(api, playbackQueue)
        recyclerView.adapter = adapter
    }

    override fun updateItems(items: List<CollectionItem>) {
        val sortedItems = items.sortedWith { a, b -> a.name.compareTo(b.name, ignoreCase = true) }
        adapter.updateItems(sortedItems)
    }

    override fun setOnRefreshListener(listener: OnRefreshListener?) {
        swipeRefresh.isEnabled = listener != null
        swipeRefresh.setOnRefreshListener { listener?.onRefresh() }
    }

    override fun saveScrollPosition(): Int {
        val layoutManger = recyclerView.layoutManager as LinearLayoutManager
        return layoutManger.findFirstCompletelyVisibleItemPosition()
    }

    override fun restoreScrollPosition(position: Int) {
        recyclerView.scrollToPosition(position)
    }
}
