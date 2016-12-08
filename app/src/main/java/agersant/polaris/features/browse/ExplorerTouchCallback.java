package agersant.polaris.features.browse;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by agersant on 12/4/2016.
 */

class ExplorerTouchCallback extends ItemTouchHelper.SimpleCallback {

    ExplorerTouchCallback() {
        super(0, ItemTouchHelper.RIGHT);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        ExplorerAdapter.BrowseItemHolder itemHolder = (ExplorerAdapter.BrowseItemHolder) viewHolder;
        itemHolder.onSwiped(itemHolder.itemView);
    }

    @Override
    public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        ExplorerAdapter.BrowseItemHolder itemHolder = (ExplorerAdapter.BrowseItemHolder) viewHolder;
        itemHolder.onChildDraw(canvas, dX, actionState);
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}