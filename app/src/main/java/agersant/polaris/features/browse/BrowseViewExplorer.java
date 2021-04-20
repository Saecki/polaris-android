package agersant.polaris.features.browse;


import android.content.Context;
import android.view.LayoutInflater;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import agersant.polaris.CollectionItem;
import agersant.polaris.PlaybackQueue;
import agersant.polaris.R;
import agersant.polaris.api.API;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class BrowseViewExplorer extends BrowseViewContent {

    private final RecyclerView recyclerView;
    private final BrowseAdapter adapter;
    private final SwipyRefreshLayout swipeRefresh;

    public BrowseViewExplorer(Context context) {
        super(context);
        throw new UnsupportedOperationException();
    }

    public BrowseViewExplorer(Context context, API api, PlaybackQueue playbackQueue) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_browse_explorer, this, true);

        recyclerView = findViewById(R.id.browse_recycler_view);
        recyclerView.setHasFixedSize(true);

        ItemTouchHelper.Callback callback = new BrowseTouchCallback();
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        adapter = new BrowseAdapterExplorer(api, playbackQueue);
        recyclerView.setAdapter(adapter);

        swipeRefresh = findViewById(R.id.swipe_refresh);
    }

    @Override
    void updateItems(List<CollectionItem> items) {
        Collections.sort(items, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        adapter.setItems(items);
    }

    @Override
    void setOnRefreshListener(SwipyRefreshLayout.OnRefreshListener listener) {
        swipeRefresh.setEnabled(listener != null);
        swipeRefresh.setOnRefreshListener(listener);
    }


    @Override
    int getScrollPosition() {
        LinearLayoutManager layoutManger = (LinearLayoutManager) recyclerView.getLayoutManager();
        return layoutManger.findFirstCompletelyVisibleItemPosition();
    }

    @Override
    void setScrollPosition(int position) {
        recyclerView.scrollToPosition(position);
    }
}
