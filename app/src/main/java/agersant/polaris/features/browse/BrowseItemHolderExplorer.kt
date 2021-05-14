package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.PlaybackQueue
import agersant.polaris.R
import agersant.polaris.api.API
import agersant.polaris.databinding.ViewBrowseExplorerItemBinding
import agersant.polaris.databinding.ViewQueueStatusBinding
import android.widget.TextView

internal class BrowseItemHolderExplorer(
    api: API,
    playbackQueue: PlaybackQueue,
    adapter: BrowseAdapter,
    itemBinding: ViewBrowseExplorerItemBinding,
    queueStatusBinding: ViewQueueStatusBinding
) : BrowseItemHolder(api, playbackQueue, adapter, itemBinding.root, queueStatusBinding) {

    private val textView: TextView = itemBinding.text

    override fun bindItem(item: CollectionItem) {
        super.bindItem(item)

        textView.text = item.name
        val res = if (item.isDirectory) {
            R.drawable.ic_folder_open_24
        } else {
            R.drawable.ic_audiotrack_24
        }
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)
    }

    init {
        itemView.setOnClickListener(this)
    }
}
