package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.PlaybackQueue
import agersant.polaris.R
import agersant.polaris.api.API
import agersant.polaris.databinding.ViewBrowseAlbumDiscHeaderBinding
import agersant.polaris.databinding.ViewQueueStatusBinding
import android.view.View
import android.widget.TextView

internal class BrowseItemHolderAlbumDiscHeader(
    api: API,
    playbackQueue: PlaybackQueue,
    adapter: BrowseAdapter,
    itemBinding: ViewBrowseAlbumDiscHeaderBinding,
    queueStatusBinding: ViewQueueStatusBinding,
) : BrowseItemHolder(api, playbackQueue, adapter, itemBinding.root, queueStatusBinding) {

    private val discText: TextView = itemBinding.disc

    override fun bindItem(item: CollectionItem) {
        throw UnsupportedOperationException()
    }

    override fun onClick(view: View) {
        throw UnsupportedOperationException()
    }

    override fun onSwiped(view: View?) {
        throw UnsupportedOperationException()
    }

    fun setDiscNumber(number: Int) {
        discText.text = itemView.context.getString(R.string.browse_disc_number, number)
    }
}
