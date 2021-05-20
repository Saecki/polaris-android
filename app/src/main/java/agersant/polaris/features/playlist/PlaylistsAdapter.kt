package agersant.polaris.features.playlist

import agersant.polaris.PlaybackQueue
import agersant.polaris.Playlist
import agersant.polaris.api.API
import agersant.polaris.databinding.ViewPlaylistsItemBinding
import agersant.polaris.databinding.ViewQueueStatusBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

internal class PlaylistsAdapter(
    private val api: API,
    private val playbackQueue: PlaybackQueue,
) : RecyclerView.Adapter<PlaylistsItemHolder>() {

    inner class DiffCallback : DiffUtil.Callback() {
        var oldItems = items

        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = items.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].name == items[newItemPosition].name
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition] == items[newItemPosition]
        }
    }

    private val diffCallback = DiffCallback()

    var items: List<Playlist> = listOf()
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistsItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemBinding = ViewPlaylistsItemBinding.inflate(inflater, parent, false)
        val queueStatusBinding = ViewQueueStatusBinding.inflate(inflater, parent, false)

        return PlaylistsItemHolder(api, playbackQueue, this, itemBinding, queueStatusBinding)
    }

    override fun onBindViewHolder(holder: PlaylistsItemHolder, position: Int) {
        holder.bindItem(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateItems(items: List<Playlist>) {
        diffCallback.oldItems = this.items
        this.items = items
        val diff = DiffUtil.calculateDiff(diffCallback)
        diff.dispatchUpdatesTo(this)
    }
}
