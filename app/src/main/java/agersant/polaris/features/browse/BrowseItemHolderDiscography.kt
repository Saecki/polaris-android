package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.PlaybackQueue
import agersant.polaris.R
import agersant.polaris.api.API
import agersant.polaris.databinding.ViewBrowseDiscographyItemBinding
import agersant.polaris.databinding.ViewQueueStatusBinding
import android.widget.ImageView
import android.widget.TextView

internal class BrowseItemHolderDiscography(
    api: API,
    playbackQueue: PlaybackQueue,
    adapter: BrowseAdapter,
    itemBinding: ViewBrowseDiscographyItemBinding,
    queueStatusBinding: ViewQueueStatusBinding,
) : BrowseItemHolder(api, playbackQueue, adapter, itemBinding.root, queueStatusBinding) {

    private val artwork: ImageView = itemBinding.artwork
    private val artist: TextView = itemBinding.artist
    private val album: TextView = itemBinding.album

    init {
        itemView.setOnClickListener(this)
    }

    override fun bindItem(item: CollectionItem) {
        super.bindItem(item)

        var artistString = item.albumArtist ?: item.artist.orEmpty()
        if (item.year != -1) {
            artistString += " • ${item.year}"
        }
        artist.text = artistString
        album.text = item.album.orEmpty()
        if (item.artwork != null) {
            api.loadImageIntoView(item, artwork)
        } else {
            artwork.setImageResource(R.drawable.ic_fallback_artwork)
        }
    }
}
