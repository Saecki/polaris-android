package agersant.polaris.features.queue

import agersant.polaris.PlaybackQueue
import agersant.polaris.PlaybackQueue.Ordering
import agersant.polaris.PolarisApplication
import agersant.polaris.PolarisPlayer
import agersant.polaris.R
import agersant.polaris.api.local.OfflineCache
import agersant.polaris.api.remote.DownloadQueue
import agersant.polaris.databinding.FragmentQueueBinding
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.util.*

class QueueFragment : Fragment() {
    private lateinit var adapter: QueueAdapter
    private lateinit var tutorial: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: Toolbar
    private lateinit var playbackQueue: PlaybackQueue
    private lateinit var player: PolarisPlayer
    private lateinit var offlineCache: OfflineCache
    private lateinit var downloadQueue: DownloadQueue
    private var receiver: BroadcastReceiver? = null
    private var initialCreation = true

    private fun subscribeToEvents() {
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
            override fun onReceive(context: Context, intent: Intent?) {
                if (intent == null || intent.action == null) {
                    return
                }
                when (intent.action) {
                    PlaybackQueue.REMOVED_ITEM,
                    PlaybackQueue.REMOVED_ITEMS -> {
                        updateTutorial()
                    }
                    PlaybackQueue.QUEUED_ITEMS,
                    PlaybackQueue.OVERWROTE_QUEUE -> {
                        adapter.notifyDataSetChanged()
                        updateTutorial()
                    }
                    PolarisPlayer.OPENING_TRACK,
                    PolarisPlayer.PLAYING_TRACK,
                    OfflineCache.AUDIO_CACHED,
                    OfflineCache.AUDIO_REMOVED_FROM_CACHE,
                    DownloadQueue.WORKLOAD_CHANGED -> {
                        adapter.notifyItemRangeChanged(0, adapter.itemCount)
                    }
                }
            }
        }
        requireActivity().registerReceiver(receiver, filter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)
        val state = PolarisApplication.getState()
        playbackQueue = state.playbackQueue
        player = state.player
        offlineCache = state.offlineCache
        downloadQueue = state.downloadQueue
        val binding = FragmentQueueBinding.inflate(inflater)
        recyclerView = binding.queueRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val animator: DefaultItemAnimator = object : DefaultItemAnimator() {
            override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
                holder.itemView.alpha = 0f
                return false
            }

            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }
        recyclerView.itemAnimator = animator
        tutorial = binding.queueTutorial
        toolbar = requireActivity().findViewById(R.id.toolbar)
        populate()
        updateTutorial()
        return binding.root
    }

    private fun updateTutorial() {
        val empty = adapter.itemCount == 0
        if (empty) {
            tutorial.visibility = View.VISIBLE
        } else {
            tutorial.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        subscribeToEvents()
        updateTutorial()
        if (!initialCreation) {
            adapter.notifyDataSetChanged()
        } else {
            initialCreation = false
        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(receiver)
        receiver = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.queue, menu)
        updateOrderingIcon()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear -> {
                showClearUndo()
                true
            }
            R.id.action_shuffle -> {
                shuffle()
                true
            }
            R.id.action_ordering_sequence, R.id.action_ordering_repeat_one, R.id.action_ordering_repeat_all -> {
                setOrdering(item)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun populate() {
        adapter = QueueAdapter(playbackQueue, player, offlineCache, downloadQueue)
        val callback: ItemTouchHelper.Callback = QueueTouchCallback(adapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerView.adapter = adapter
    }

    private fun showClearUndo() {
        if (playbackQueue.isEmpty) return

        val oldItems = playbackQueue.content

        Snackbar.make(requireContext(), toolbar, getString(R.string.queue_cleared), Snackbar.LENGTH_LONG)
            .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
            .setAction(R.string.undo) {
                playbackQueue.addItems(oldItems)
                adapter.notifyDataSetChanged()
            }
            .show()

        clear()
    }

    private fun clear() {
        val oldCount = adapter.itemCount
        playbackQueue.clear()
        adapter.notifyItemRangeRemoved(0, oldCount)
    }

    private fun shuffle() {
        val rng = Random()
        val count = adapter.itemCount
        for (i in 0..count - 2) {
            val j = i + rng.nextInt(count - i)
            playbackQueue.move(i, j)
            adapter.notifyItemMoved(i, j)
        }
    }

    private fun setOrdering(item: MenuItem) {
        when (item.itemId) {
            R.id.action_ordering_sequence -> playbackQueue.ordering = Ordering.SEQUENCE
            R.id.action_ordering_repeat_one -> playbackQueue.ordering = Ordering.REPEAT_ONE
            R.id.action_ordering_repeat_all -> playbackQueue.ordering = Ordering.REPEAT_ALL
        }
        updateOrderingIcon()
    }

    private fun getIconForOrdering(ordering: Ordering): Int {
        return when (ordering) {
            Ordering.REPEAT_ONE -> R.drawable.ic_repeat_one_24
            Ordering.REPEAT_ALL -> R.drawable.ic_repeat_24
            Ordering.SEQUENCE -> R.drawable.ic_reorder_24
        }
    }

    private fun updateOrderingIcon() {
        val icon = getIconForOrdering(playbackQueue.ordering)
        val orderingItem = toolbar.menu.findItem(R.id.action_ordering)
        orderingItem?.setIcon(icon)
    }
}
