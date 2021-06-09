package agersant.polaris.features.queue

import agersant.polaris.CollectionItem
import agersant.polaris.PlaybackQueue
import agersant.polaris.PolarisPlayer
import agersant.polaris.api.API
import agersant.polaris.api.local.OfflineCache
import agersant.polaris.api.remote.DownloadQueue
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

internal class QueueAdapter(
    private val playbackQueue: PlaybackQueue,
    private val player: PolarisPlayer,
    private val api: API,
    private val offlineCache: OfflineCache,
    private val downloadQueue: DownloadQueue
) : RecyclerView.Adapter<QueueItemHolder>() {

    inner class DiffCallback : DiffUtil.Callback() {
        var oldItems: List<CollectionItem> = listOf()

        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = playbackQueue.content.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].path == playbackQueue.content[newItemPosition].path
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].path == playbackQueue.content[newItemPosition].path
        }
    }

    private val diffCallback = DiffCallback()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueItemHolder {
        val queueItemView = QueueItemView(parent.context)
        queueItemView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return QueueItemHolder(queueItemView, player, api, offlineCache, downloadQueue)
    }

    override fun onBindViewHolder(holder: QueueItemHolder, position: Int) {
        holder.bindItem(playbackQueue.getItem(position))
    }

    override fun getItemCount(): Int {
        return playbackQueue.size
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        playbackQueue.swap(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun onItemDismiss(position: Int) {
        playbackQueue.remove(position)
        notifyItemRemoved(position)
    }

    fun updateItems() {
        val diff = DiffUtil.calculateDiff(diffCallback)
        diff.dispatchUpdatesTo(this)
        diffCallback.oldItems = this.playbackQueue.content
    }
}
