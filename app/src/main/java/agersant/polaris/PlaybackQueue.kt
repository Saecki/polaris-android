package agersant.polaris

import agersant.polaris.api.local.OfflineCache
import agersant.polaris.api.remote.DownloadQueue
import android.content.Intent
import androidx.preference.PreferenceManager
import java.util.*
import kotlin.math.max
import kotlin.math.min

class PlaybackQueue internal constructor() {

    enum class Ordering {
        SEQUENCE, REPEAT_ONE, REPEAT_ALL
    }

    companion object {
        const val CHANGED_ORDERING = "CHANGED_ORDERING"
        const val QUEUED_ITEM = "QUEUED_ITEM"
        const val QUEUED_ITEMS = "QUEUED_ITEMS"
        const val OVERWROTE_QUEUE = "OVERWROTE_QUEUE"
        const val NO_LONGER_EMPTY = "NO_LONGER_EMPTY"
        const val REMOVED_ITEM = "REMOVED_ITEM"
        const val REMOVED_ITEMS = "REMOVED_ITEMS"
        const val REORDERED_ITEMS = "REORDERED_ITEMS"
    }

    private var mContent = mutableListOf<CollectionItem>()

    var ordering: Ordering = Ordering.SEQUENCE
        set(value) {
            field = value
            broadcast(CHANGED_ORDERING)
        }

    var content: List<CollectionItem>
        get() = mContent.toList()
        set(value) {
            mContent = value.toMutableList()
            broadcast(OVERWROTE_QUEUE)
        }

    val size: Int
        @JvmName("size")
        get() = mContent.size

    val isEmpty: Boolean
        get() = mContent.isEmpty()

    // Return negative value if a is going to play before b, positive if a is going to play after b
    fun comparePriorities(currentItem: CollectionItem?, a: CollectionItem, b: CollectionItem): Int {
        val currentIndex = mContent.indexOf(currentItem)
        val playlistSize = mContent.size
        var scoreA = playlistSize + 1
        var scoreB = playlistSize + 1
        for (i in 0 until playlistSize) {
            val item = mContent[i]
            val path = item.path
            val score = (playlistSize + i - currentIndex) % playlistSize
            if (score < scoreA && path == a.path) {
                scoreA = score
            }
            if (score < scoreB && path == b.path) {
                scoreB = score
            }
        }
        return scoreA - scoreB
    }

    private fun addItemInternal(item: CollectionItem) {
        mContent.removeIf { it.path == item.path }
        mContent.add(item)
    }

    fun addItems(items: List<CollectionItem>) {
        val wasEmpty = size == 0
        for (item in items) {
            addItemInternal(item)
        }
        broadcast(QUEUED_ITEMS)
        if (wasEmpty) {
            broadcast(NO_LONGER_EMPTY)
        }
    }

    fun addItem(item: CollectionItem) {
        val wasEmpty = size == 0
        addItemInternal(item)
        broadcast(QUEUED_ITEM)
        if (wasEmpty) {
            broadcast(NO_LONGER_EMPTY)
        }
    }

    fun remove(position: Int) {
        mContent.removeAt(position)
        broadcast(REMOVED_ITEM)
    }

    fun clear() {
        mContent.clear()
        broadcast(REMOVED_ITEMS)
    }

    fun swap(fromPosition: Int, toPosition: Int) {
        Collections.swap(mContent, fromPosition, toPosition)
        broadcast(REORDERED_ITEMS)
    }

    fun move(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) {
            return
        }
        val low = min(fromPosition, toPosition)
        val high = max(fromPosition, toPosition)
        val distance = if (fromPosition < toPosition) -1 else 1
        Collections.rotate(mContent.subList(low, high + 1), distance)
        broadcast(REORDERED_ITEMS)
    }

    fun getItem(position: Int): CollectionItem {
        return mContent[position]
    }

    fun getNextTrack(from: CollectionItem?, delta: Int): CollectionItem? {
        if (mContent.isEmpty()) {
            return null
        }
        return if (ordering == Ordering.REPEAT_ONE) {
            from
        } else {
            val currentIndex = mContent.indexOf(from)
            if (currentIndex < 0) {
                mContent[0]
            } else {
                val newIndex = currentIndex + delta
                if (newIndex >= 0 && newIndex < mContent.size) {
                    mContent[newIndex]
                } else if (ordering == Ordering.REPEAT_ALL) {
                    if (delta > 0) {
                        mContent[0]
                    } else {
                        mContent[mContent.size - 1]
                    }
                } else {
                    null
                }
            }
        }
    }

    fun hasNextTrack(currentItem: CollectionItem?): Boolean {
        return getNextTrack(currentItem, 1) != null
    }

    fun hasPreviousTrack(currentItem: CollectionItem?): Boolean {
        return getNextTrack(currentItem, -1) != null
    }

    private fun broadcast(event: String) {
        val application = PolarisApplication.getInstance()
        val intent = Intent()
        intent.action = event
        application.sendBroadcast(intent)
    }

    fun getNextItemToDownload(currentItem: CollectionItem?, offlineCache: OfflineCache, downloadQueue: DownloadQueue): CollectionItem? {
        val currentIndex = max(0, mContent.indexOf(currentItem))

        var bestScore = 0
        var bestItem: CollectionItem? = null

        val playlistSize = mContent.size

        for (i in 0 until playlistSize) {
            val score = (playlistSize + i - currentIndex) % playlistSize
            if (bestItem != null && score > bestScore) {
                continue
            }
            val item = mContent[i]
            if (item === currentItem) {
                continue
            }
            if (offlineCache.hasAudio(item.path)) {
                continue
            }
            if (downloadQueue.isDownloading(item)) {
                continue
            }
            bestScore = score
            bestItem = item
        }

        val application = PolarisApplication.getInstance()
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        val resources = application.resources
        val numSongsToPreloadKey = resources.getString(R.string.pref_key_num_songs_preload)
        val numSongsToPreloadString = preferences.getString(numSongsToPreloadKey, "0")
        val numSongsToPreload = numSongsToPreloadString!!.toInt()
        if (numSongsToPreload in 0 until bestScore) {
            bestItem = null
        }

        return bestItem
    }
}
