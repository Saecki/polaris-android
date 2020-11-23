package agersant.polaris.features.queue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Random;

import agersant.polaris.App;
import agersant.polaris.PlaybackQueue;
import agersant.polaris.PolarisPlayer;
import agersant.polaris.PolarisState;
import agersant.polaris.R;
import agersant.polaris.api.local.OfflineCache;
import agersant.polaris.api.remote.DownloadQueue;
import agersant.polaris.databinding.FragmentQueueBinding;

public class QueueFragment extends Fragment {

    private QueueAdapter adapter;
    private BroadcastReceiver receiver;
    private FragmentQueueBinding binding;
    private PlaybackQueue playbackQueue;
    private PolarisPlayer player;
    private OfflineCache offlineCache;
    private DownloadQueue downloadQueue;

    private void subscribeToEvents() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackQueue.REMOVED_ITEM);
        filter.addAction(PlaybackQueue.REMOVED_ITEMS);
        filter.addAction(PlaybackQueue.QUEUED_ITEMS);
        filter.addAction(PolarisPlayer.OPENING_TRACK);
        filter.addAction(PolarisPlayer.PLAYING_TRACK);
        filter.addAction(OfflineCache.AUDIO_CACHED);
        filter.addAction(DownloadQueue.WORKLOAD_CHANGED);
        filter.addAction(OfflineCache.AUDIO_REMOVED_FROM_CACHE);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) {
                    return;
                }
                switch (intent.getAction()) {
                    case PlaybackQueue.REMOVED_ITEM:
                    case PlaybackQueue.REMOVED_ITEMS:
                        updateTutorial();
                        break;
                    case PlaybackQueue.QUEUED_ITEMS:
                    case PlaybackQueue.OVERWROTE_QUEUE:
                        adapter.notifyDataSetChanged();
                        updateTutorial();
                        break;
                    case PolarisPlayer.OPENING_TRACK:
                    case PolarisPlayer.PLAYING_TRACK:
                    case OfflineCache.AUDIO_CACHED:
                    case OfflineCache.AUDIO_REMOVED_FROM_CACHE:
                    case DownloadQueue.WORKLOAD_CHANGED:
                        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
                        break;
                }
            }
        };
        App.instance.registerReceiver(receiver, filter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PolarisState state = App.state;
        playbackQueue = state.playbackQueue;
        player = state.player;
        offlineCache = state.offlineCache;
        downloadQueue = state.downloadQueue;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQueueBinding.inflate(inflater);

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(App.instance));
        DefaultItemAnimator animator = new DefaultItemAnimator() {
            @Override
            public boolean animateRemove(RecyclerView.ViewHolder holder) {
                holder.itemView.setAlpha(0.f);
                return false;
            }

            @Override
            public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
                return true;
            }
        };
        binding.recyclerView.setItemAnimator(animator);

        populate();
        updateTutorial();

        return binding.getRoot();
    }

    private void updateTutorial() {
        boolean empty = adapter.getItemCount() == 0;
        if (empty) {
            binding.tutorial.setVisibility(View.VISIBLE);
        } else {
            binding.tutorial.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        subscribeToEvents();
        updateTutorial();
    }

    @Override
    public void onStop() {
        super.onStop();
        App.instance.unregisterReceiver(receiver);
        receiver = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        updateOrderingIcon();
        updateTutorial();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_queue, menu);
        updateOrderingIcon();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                clear();
                return true;
            case R.id.action_shuffle:
                shuffle();
                return true;
            case R.id.action_ordering_sequence:
            case R.id.action_ordering_repeat_one:
            case R.id.action_ordering_repeat_all:
                setOrdering(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void populate() {
        adapter = new QueueAdapter(playbackQueue, player, offlineCache, downloadQueue);
        ItemTouchHelper.Callback callback = new QueueTouchCallback(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);
        binding.recyclerView.setAdapter(adapter);
    }

    private void clear() {
        int oldCount = adapter.getItemCount();
        playbackQueue.clear();
        adapter.notifyItemRangeRemoved(0, oldCount);
    }

    private void shuffle() {
        Random rng = new Random();
        int count = adapter.getItemCount();
        for (int i = 0; i <= count - 2; i++) {
            int j = i + rng.nextInt(count - i);
            playbackQueue.move(i, j);
            adapter.notifyItemMoved(i, j);
        }
    }

    private void setOrdering(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_ordering_sequence:
                playbackQueue.setOrdering(PlaybackQueue.Ordering.SEQUENCE);
                break;
            case R.id.action_ordering_repeat_one:
                playbackQueue.setOrdering(PlaybackQueue.Ordering.REPEAT_ONE);
                break;
            case R.id.action_ordering_repeat_all:
                playbackQueue.setOrdering(PlaybackQueue.Ordering.REPEAT_ALL);
                break;
        }
        updateOrderingIcon();
    }

    private int getIconForOrdering(PlaybackQueue.Ordering ordering) {
        switch (ordering) {
            case REPEAT_ONE:
                return R.drawable.ic_repeat_one_white_24dp;
            case REPEAT_ALL:
                return R.drawable.ic_repeat_white_24dp;
            case SEQUENCE:
            default:
                return R.drawable.ic_reorder_white_24dp;
        }
    }

    private void updateOrderingIcon() {
        int icon = getIconForOrdering(playbackQueue.getOrdering());
        MenuItem orderingItem = binding.toolbar.getMenu().findItem(R.id.action_ordering);
        if (orderingItem != null) {
            orderingItem.setIcon(icon);
        }
    }
}
