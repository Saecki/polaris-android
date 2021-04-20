package agersant.polaris.features.browse;

import android.view.LayoutInflater
import android.view.ViewGroup

import agersant.polaris.PlaybackQueue
import agersant.polaris.R
import agersant.polaris.api.API


internal class BrowseAdapterExplorer(
    private val api: API,
    private val playbackQueue: PlaybackQueue
) : BrowseAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemQueueStatusView = inflater.inflate(R.layout.view_browse_item_queued, parent, false);
        val itemView = inflater.inflate(R.layout.view_browse_explorer_item, parent, false);

        return BrowseItemHolderExplorer(api, playbackQueue, this, itemView, itemQueueStatusView);
    }
}
