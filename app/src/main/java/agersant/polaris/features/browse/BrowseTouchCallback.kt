package agersant.polaris.features.browse

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

internal open class BrowseTouchCallback() : ItemTouchHelper.SimpleCallback(0, 0) {

    private var enableRefresh: (Boolean) -> Unit = {}

    override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return when (viewHolder) {
            is BrowseItemHolderAlbumDiscHeader -> 0
            else -> ItemTouchHelper.RIGHT
        }
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val itemHolder = viewHolder as BrowseItemHolder
        itemHolder.onSwiped()
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        val isNotSwiping = actionState != ItemTouchHelper.ACTION_STATE_SWIPE
        enableRefresh(isNotSwiping)
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemHolder = viewHolder as BrowseItemHolder
        itemHolder.onChildDraw(canvas, dX, actionState)
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    fun setOnEnableRefresh(enable: (Boolean) -> Unit) {
        this.enableRefresh = enable
    }
}
