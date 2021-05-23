package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.Directory
import agersant.polaris.PlaybackQueue
import agersant.polaris.R
import agersant.polaris.Song
import agersant.polaris.api.API
import agersant.polaris.api.ThumbnailSize
import android.view.View
import android.widget.ImageView
import android.widget.TextView

internal class BrowseItemHolderDiscography(
    api: API?,
    playbackQueue: PlaybackQueue?,
    adapter: BrowseAdapter?,
    itemView: View,
    itemQueueStatusView: View?
) : BrowseItemHolder(api, playbackQueue, adapter, itemView, itemQueueStatusView) {

    private val artwork: ImageView = itemView.findViewById(R.id.artwork)
    private val artist: TextView = itemView.findViewById(R.id.artist)
    private val album: TextView = itemView.findViewById(R.id.album)

    init {
        itemView.setOnClickListener(this)
    }

    public override fun bindItem(item: CollectionItem) {
        super.bindItem(item)

        var artistString = item.artist.orEmpty()
        if (item.year != -1) {
            artistString += " • ${item.year}"
        }
        artist.text = artistString
        album.text = item.album.orEmpty()
        if (item.artwork != null) {
            api.loadThumbnailIntoView(item, ThumbnailSize.Small, artwork)
        } else {
            artwork.setImageResource(R.drawable.ic_fallback_artwork)
        }
    }
}
