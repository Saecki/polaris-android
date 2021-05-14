package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.PlaybackQueue
import agersant.polaris.R
import agersant.polaris.api.API
import agersant.polaris.api.ItemsCallback
import agersant.polaris.databinding.ViewQueueStatusBinding
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.ceil

internal abstract class BrowseItemHolder(
    protected val api: API,
    protected val playbackQueue: PlaybackQueue,
    protected val adapter: BrowseAdapter,
    itemView: View,
    queueStatusBinding: ViewQueueStatusBinding
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private val queueStatusView = queueStatusBinding.root
    private val queueStatusText = queueStatusBinding.statusText
    private val queueStatusIcon = queueStatusBinding.statusIcon
    protected var item: CollectionItem? = null

    open fun bindItem(item: CollectionItem) {
        this.item = item
        setStatusToIdle()
    }

    override fun onClick(view: View) {
        if (item!!.isDirectory) {
            val args = Bundle()
            args.putSerializable(BrowseFragment.NAVIGATION_MODE, BrowseFragment.NavigationMode.PATH)
            args.putString(BrowseFragment.PATH, item!!.path)
            Navigation.findNavController(view).navigate(R.id.nav_browse, args)
        }
    }

    open fun onSwiped(view: View?) {
        item?.let {
            if (it.isDirectory) {
                queueDirectory()
                setStatusToFetching()
            } else {
                playbackQueue.addItem(it)
                setStatusToQueued()
            }
        }
    }

    private fun queueDirectory() {
        val fetchingItem = item
        val handlers: ItemsCallback = object : ItemsCallback {
            override fun onSuccess(items: List<CollectionItem>) {
                Handler(Looper.getMainLooper()).post {
                    playbackQueue.addItems(items)
                    if (item === fetchingItem) {
                        setStatusToQueued()
                    }
                }
            }

            override fun onError() {
                Handler(Looper.getMainLooper()).post {
                    if (item === fetchingItem) {
                        setStatusToQueueError()
                    }
                }
            }
        }
        api.flatten(item!!.path, handlers)
    }

    private fun setStatusToIdle() {
        queueStatusText.setText(R.string.add_to_queue)
        queueStatusIcon.setImageResource(R.drawable.ic_playlist_play_24)
        itemView.requestLayout()
    }

    private fun setStatusToFetching() {
        queueStatusText.setText(R.string.queuing)
        queueStatusIcon.setImageResource(R.drawable.ic_hourglass_empty_24)
        itemView.requestLayout()
    }

    private fun setStatusToQueued() {
        queueStatusText.setText(R.string.queued)
        queueStatusIcon.setImageResource(R.drawable.ic_check_24)
        itemView.requestLayout()
        waitAndSwipeBack()
    }

    private fun setStatusToQueueError() {
        queueStatusText.setText(R.string.queuing_error)
        queueStatusIcon.setImageResource(R.drawable.ic_error_24)
        itemView.requestLayout()
        waitAndSwipeBack()
    }

    private fun waitAndSwipeBack() {
        val oldItem = item
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (item === oldItem) {
                val position = adapterPosition
                adapter.notifyItemChanged(position)
            }
        }, 1000)
    }

    fun onChildDraw(canvas: Canvas, dX: Float, actionState: Int) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val widthSpec = View.MeasureSpec.makeMeasureSpec(itemView.width, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(itemView.height, View.MeasureSpec.EXACTLY)
            queueStatusView.measure(widthSpec, heightSpec)
            queueStatusView.layout(0, 0, queueStatusView.measuredWidth, queueStatusView.measuredHeight)

            canvas.save()
            canvas.translate(itemView.left.toFloat(), itemView.top.toFloat())
            canvas.clipRect(0, 0, ceil(dX.toDouble()).toInt(), queueStatusView.measuredHeight)
            queueStatusView.draw(canvas)
            canvas.restore()
        }
    }
}
