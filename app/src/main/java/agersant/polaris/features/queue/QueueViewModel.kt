package agersant.polaris.features.queue

import agersant.polaris.*
import agersant.polaris.api.local.OfflineCache
import agersant.polaris.api.remote.DownloadQueue
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QueueViewModel : ViewModel() {
    var receiver: BroadcastReceiver? = null
    var state: PolarisState = App.state

    var lastPlayingTrack = playingTrackPositon()
    val playingTrack = MutableLiveData(playingTrackPositon())
    val items = MutableLiveData<List<CollectionItem>>(state.playbackQueue.items.toList())
    val itemsState = MutableLiveData(Unit)
    val ordering = MutableLiveData(state.playbackQueue.ordering)

    init {
        subscribeEvents()
    }

    fun clear() {
        state.playbackQueue.clear()
        items.value = listOf()
    }

    fun shuffle() {
        state.playbackQueue.items.shuffle()
        items.value = state.playbackQueue.items.toList()
    }

    fun setOrdering(ordering: PlaybackQueue.Ordering) {
        this.ordering.value = ordering
        state.playbackQueue.ordering = ordering
    }

    private fun playingTrackPositon(): Int {
        return state.playbackQueue.items.indexOf(state.player.currentItem)
    }

    private fun subscribeEvents() {
        val filter = IntentFilter()
        filter.addAction(PlaybackQueue.REMOVED_ITEM)
        filter.addAction(PlaybackQueue.REMOVED_ITEMS)
        filter.addAction(PlaybackQueue.QUEUED_ITEMS)
        filter.addAction(PolarisPlayer.OPENING_TRACK)
        filter.addAction(PolarisPlayer.PLAYING_TRACK)
        filter.addAction(OfflineCache.AUDIO_CACHED)
        filter.addAction(DownloadQueue.WORKLOAD_CHANGED)
        filter.addAction(OfflineCache.AUDIO_REMOVED_FROM_CACHE)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == null) {
                    return
                }
                when (intent.action) {
                    PolarisPlayer.OPENING_TRACK,
                    PolarisPlayer.PLAYING_TRACK,
                    -> {
                        playingTrack.value = playingTrackPositon()
                    }
                    PlaybackQueue.REMOVED_ITEM,
                    PlaybackQueue.REMOVED_ITEMS,
                    PlaybackQueue.QUEUED_ITEMS,
                    PlaybackQueue.OVERWROTE_QUEUE,
                    -> items.value = state.playbackQueue.items.toList()
                    OfflineCache.AUDIO_CACHED,
                    OfflineCache.AUDIO_REMOVED_FROM_CACHE,
                    DownloadQueue.WORKLOAD_CHANGED,
                    -> itemsState.value = Unit
                }
            }
        }
        App.instance.registerReceiver(receiver, filter)
    }
}