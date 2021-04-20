package agersant.polaris.features.browse;

import agersant.polaris.PlaybackQueue
import agersant.polaris.R
import agersant.polaris.api.API
import android.view.LayoutInflater
import android.view.ViewGroup


internal class BrowseAdapterDiscography(
    private val api: API,
    private val playbackQueue: PlaybackQueue,
) : BrowseAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemQueueStatusView = inflater.inflate(R.layout.view_browse_item_queued, parent, false);
        val itemView = inflater.inflate(R.layout.view_browse_discography_item, parent, false);

        return BrowseItemHolderDiscography(api, playbackQueue, this, itemView, itemQueueStatusView);
    }
}
