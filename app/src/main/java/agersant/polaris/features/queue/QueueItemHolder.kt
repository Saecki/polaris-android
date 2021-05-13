package agersant.polaris.features.queue

import agersant.polaris.CollectionItem
import agersant.polaris.PolarisPlayer
import agersant.polaris.R
import agersant.polaris.api.API
import agersant.polaris.api.local.OfflineCache
import agersant.polaris.api.remote.DownloadQueue
import agersant.polaris.databinding.ViewQueueItemBinding
import agersant.polaris.util.formatDuration
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

internal class QueueItemHolder(
    private val queueItemView: QueueItemView,
    private val player: PolarisPlayer,
    private val api: API,
    private val offlineCache: OfflineCache,
    private val downloadQueue: DownloadQueue
) : RecyclerView.ViewHolder(queueItemView), View.OnClickListener {

    private val titleText: TextView
    private val artistText: TextView
    private val durationText: TextView
    private val artwork: ImageView
    private val statusIcon: ImageView
    private var item: CollectionItem? = null
    private var state: QueueItemState? = null

    init {
        val binding = ViewQueueItemBinding.bind(queueItemView.getChildAt(0))
        titleText = binding.title
        artistText = binding.artist
        durationText = binding.duration
        artwork = binding.artwork
        statusIcon = binding.statusIcon

        queueItemView.setOnClickListener(this)
    }

    fun bindItem(item: CollectionItem) {
        val isNewItem = item != this.item
        this.item = item

        if (isNewItem) {
            titleText.text = item.title
            artistText.text = item.artist
            durationText.text = formatDuration(item.duration)
            if (item.artwork != null) {
                api.loadImageIntoView(item, artwork)
            } else {
                artwork.setImageResource(R.drawable.ic_fallback_artwork)
            }
            updateState(QueueItemState.IDLE)
        }

        val isPlaying = player.currentItem == item
        queueItemView.setIsPlaying(isPlaying)

        beginStateUpdate(item)
    }

    private fun beginStateUpdate(item: CollectionItem) {
        MainScope().launch {
            val initialItem = WeakReference(item)

            val state = withContext(Dispatchers.IO) {
                if (offlineCache.hasAudio(item.path)) {
                    QueueItemState.CACHED
                } else if (downloadQueue.isDownloading(item) || downloadQueue.isStreaming(item)) {
                    QueueItemState.DOWNLOADING
                } else {
                    QueueItemState.IDLE
                }
            }

            if (initialItem.get() == item) {
                updateState(state)
            }
        }
    }

    private fun updateState(newState: QueueItemState) {
        state = newState
        when (state) {
            QueueItemState.IDLE -> {
                statusIcon.visibility = View.INVISIBLE
            }
            QueueItemState.DOWNLOADING -> {
                statusIcon.setImageResource(R.drawable.ic_sync_24)
                statusIcon.visibility = View.VISIBLE
            }
            QueueItemState.CACHED -> {
                statusIcon.setImageResource(R.drawable.ic_check_circle_24)
                statusIcon.visibility = View.VISIBLE
            }
        }
    }

    override fun onClick(view: View) {
        player.play(item)
    }
}
