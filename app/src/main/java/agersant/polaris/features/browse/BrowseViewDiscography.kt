package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.PlaybackQueue
import agersant.polaris.api.API
import agersant.polaris.databinding.ViewBrowseDiscographyBinding
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.ItemTouchHelper
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout
import java.util.*

@SuppressLint("ViewConstructor")
internal class BrowseViewDiscography(
    context: Context,
    api: API?,
    playbackQueue: PlaybackQueue?,
    private val sortAlbums: Boolean = false,
) : BrowseViewContent(context) {

    private val swipeRefresh: SwipyRefreshLayout
    private val adapter: BrowseAdapter

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ViewBrowseDiscographyBinding.inflate(inflater, this, true)
        swipeRefresh = binding.swipeRefresh

        val recyclerView = binding.browseRecyclerView
        recyclerView.setHasFixedSize(true)

        val callback: ItemTouchHelper.Callback = BrowseTouchCallback()
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        adapter = BrowseAdapterDiscography(api, playbackQueue)
        recyclerView.adapter = adapter
    }

    public override fun setItems(items: ArrayList<out CollectionItem>) {
        if (sortAlbums) items.sortBy { it.year }
        adapter.setItems(items)
    }

    override fun setOnRefreshListener(listener: SwipyRefreshLayout.OnRefreshListener?) {
        swipeRefresh.isEnabled = listener != null
        swipeRefresh.setOnRefreshListener(listener)
    }
}
