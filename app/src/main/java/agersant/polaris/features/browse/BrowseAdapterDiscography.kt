package agersant.polaris.features.browse;

import agersant.polaris.PlaybackQueue
import agersant.polaris.api.API
import agersant.polaris.databinding.ViewBrowseDiscographyItemBinding
import agersant.polaris.databinding.ViewQueueStatusBinding
import android.view.LayoutInflater
import android.view.ViewGroup


internal class BrowseAdapterDiscography(
    private val api: API,
    private val playbackQueue: PlaybackQueue,
) : BrowseAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemBinding = ViewBrowseDiscographyItemBinding.inflate(inflater, parent, false);
        val itemQueueStatusView = ViewQueueStatusBinding.inflate(inflater, parent, false);

        return BrowseItemHolderDiscography(api, playbackQueue, this, itemBinding, itemQueueStatusView);
    }
}
