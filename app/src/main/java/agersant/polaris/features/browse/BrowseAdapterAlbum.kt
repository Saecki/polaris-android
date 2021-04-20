package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.PlaybackQueue
import agersant.polaris.R
import agersant.polaris.api.API
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.ViewGroup

internal class BrowseAdapterAlbum(
    private val api: API,
    private val playbackQueue: PlaybackQueue,
) : BrowseAdapter() {
    // Key is disc number, value is number of tracks
    private val discSizes = SparseIntArray()
    private var numDiscHeaders = 0

    override fun updateItems(items: List<CollectionItem>) {
        for (item in items) {
            val discNumber = item.discNumber
            discSizes.put(discNumber, discSizes[discNumber, 0] + 1)
        }
        numDiscHeaders = discSizes.size()
        if (numDiscHeaders == 1) {
            numDiscHeaders = 0
        }

        this.items = items
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + numDiscHeaders
    }

    override fun getItemViewType(position: Int): Int {
        var currentDiscStart = 0
        for (discIndex in 0 until numDiscHeaders) {
            val currentDiscSize = discSizes.valueAt(discIndex)

            if (position == currentDiscStart) {
                return AlbumViewType.DISC_HEADER.ordinal
            } else if (position < currentDiscStart + currentDiscSize + 1) {
                return AlbumViewType.TRACK.ordinal
            }
            currentDiscStart += currentDiscSize + 1
        }

        return AlbumViewType.TRACK.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseItemHolder {
        val itemQueueStatusView = LayoutInflater.from(parent.context).inflate(R.layout.view_browse_item_queued, parent, false)

        return when (viewType) {
            AlbumViewType.DISC_HEADER.ordinal -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.view_browse_album_disc_header, parent, false)
                BrowseItemHolderAlbumDiscHeader(api, playbackQueue, this, itemView, itemQueueStatusView)
            }
            else -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.view_browse_album_item, parent, false)
                BrowseItemHolderAlbumTrack(api, playbackQueue, this, itemView, itemQueueStatusView)
            }
        }
    }

    override fun onBindViewHolder(holder: BrowseItemHolder, position: Int) {
        when (holder) {
            is BrowseItemHolderAlbumTrack -> {

                // Assign track item
                if (numDiscHeaders > 0) {
                    var offset = 1
                    var currentDiscStart = 0
                    for (discIndex in 0 until numDiscHeaders) {
                        val currentDiscSize = discSizes.valueAt(discIndex)
                        if (position < currentDiscStart + currentDiscSize + 1) {
                            break
                        }
                        currentDiscStart += currentDiscSize + 1
                        offset += 1
                    }
                    holder.bindItem(items[position - offset])
                } else {
                    holder.bindItem(items[position])
                }
            }
            is BrowseItemHolderAlbumDiscHeader -> {

                // Assign disc number
                var currentDiscStart = 0
                for (discIndex in 0 until numDiscHeaders) {
                    val currentDiscSize = discSizes.valueAt(discIndex)
                    if (position == currentDiscStart) {
                        holder.setDiscNumber(discSizes.keyAt(discIndex))
                        break
                    }
                    currentDiscStart += currentDiscSize + 1
                }
            }
        }
    }

    internal enum class AlbumViewType {
        DISC_HEADER,
        TRACK,
    }
}