package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.PlaybackQueue
import agersant.polaris.api.API
import agersant.polaris.databinding.ViewBrowseAlbumItemBinding
import agersant.polaris.databinding.ViewQueueStatusBinding
import agersant.polaris.util.formatDuration

internal class BrowseItemHolderAlbumTrack(
    api: API,
    playbackQueue: PlaybackQueue,
    adapter: BrowseAdapter,
    itemBinding: ViewBrowseAlbumItemBinding,
    queueStatusBinding: ViewQueueStatusBinding
) : BrowseItemHolder(api, playbackQueue, adapter, itemBinding.root, queueStatusBinding) {

    private val trackNumberText = itemBinding.trackNumber
    private val titleText = itemBinding.title
    private val artistText = itemBinding.artist
    private val durationText = itemBinding.duration

    override fun bindItem(item: CollectionItem) {
        super.bindItem(item)

        if (item.trackNumber != -1) {
            trackNumberText.text = String.format("%02d", item.trackNumber)
        } else {
            trackNumberText.text = ""
        }
        titleText.text = item.title ?: item.name
        artistText.text = item.artist.orEmpty()
        durationText.text = formatDuration(item.duration)
    }
}
