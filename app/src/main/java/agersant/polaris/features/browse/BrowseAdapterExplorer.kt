package agersant.polaris.features.browse;

import agersant.polaris.PlaybackQueue
import agersant.polaris.api.API
import agersant.polaris.databinding.ViewBrowseExplorerItemBinding
import agersant.polaris.databinding.ViewQueueStatusBinding
import android.view.LayoutInflater
import android.view.ViewGroup


internal class BrowseAdapterExplorer(
    private val api: API,
    private val playbackQueue: PlaybackQueue
) : BrowseAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemBinding = ViewBrowseExplorerItemBinding.inflate(inflater, parent, false);
        val queueStatusBinding = ViewQueueStatusBinding.inflate(inflater, parent, false);

        return BrowseItemHolderExplorer(api, playbackQueue, this, itemBinding, queueStatusBinding);
    }
}
