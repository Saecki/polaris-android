package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.PlaybackQueue
import agersant.polaris.R
import agersant.polaris.Song
import agersant.polaris.api.API
import agersant.polaris.util.formatDuration
import android.view.View
import android.widget.TextView

internal class BrowseItemHolderAlbumTrack(
    api: API?,
    playbackQueue: PlaybackQueue?,
    adapter: BrowseAdapter?,
    itemView: View,
    itemQueueStatusView: View?
) : BrowseItemHolder(api, playbackQueue, adapter, itemView, itemQueueStatusView) {

    private val trackNumberText: TextView = itemView.findViewById(R.id.track_number)
    private val titleText: TextView = itemView.findViewById(R.id.title)
    private val artistText: TextView = itemView.findViewById(R.id.artist)
    private val durationText: TextView = itemView.findViewById(R.id.duration)

    public override fun bindItem(item: CollectionItem) {
        super.bindItem(item)
        val song = item as Song

        val trackNumber = song.trackNumber
        if (trackNumber != -1) {
            trackNumberText.text = String.format("%02d", trackNumber)
        } else {
            trackNumberText.text = ""
        }
        titleText.text = song.title ?: song.name
        artistText.text = song.artist.orEmpty()
        durationText.text = formatDuration(song.duration)
    }
}
