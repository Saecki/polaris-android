package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.PlaybackQueue
import agersant.polaris.R
import agersant.polaris.api.API
import agersant.polaris.databinding.ViewBrowseAlbumBinding
import agersant.polaris.util.getAttrColor
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.os.postDelayed
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

@SuppressLint("ViewConstructor")
internal class BrowseViewAlbum(
    context: Context,
    private val api: API,
    playbackQueue: PlaybackQueue,
) : BrowseViewContent(context) {

    private val recyclerView: RecyclerView
    private val motionLayout: MotionLayout?
    private val headerBackground: View?
    private val sidebarBackground: View?
    private val artwork: ImageView
    private val artist: TextView
    private val title: TextView
    private val queueAll: ExtendedFloatingActionButton
    private val divider: View?
    private val adapter: BrowseAdapter

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ViewBrowseAlbumBinding.inflate(inflater, this, true)
        recyclerView = binding.browseRecyclerView
        motionLayout = binding.motionLayout
        headerBackground = binding.headerBackground
        sidebarBackground = binding.sidebarBackground
        artwork = binding.albumArtwork
        artist = binding.albumArtist
        title = binding.albumTitle
        queueAll = binding.queueAll
        divider = binding.headerDivider

        val recyclerView = binding.browseRecyclerView
        recyclerView.setHasFixedSize(true)

        val callback: ItemTouchHelper.Callback = BrowseTouchCallback()
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        adapter = BrowseAdapterAlbum(api, playbackQueue)
        recyclerView.adapter = adapter

        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                recyclerView.setOnScrollChangeListener { v, _, _, _, _ ->
                    updateDividerVisibility()
                }
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                recyclerView.setOnScrollChangeListener { _, _, _, _, oldScrollY ->
                    if (oldScrollY < 0) {
                        queueAll.shrink()
                    } else if (oldScrollY > 0) {
                        queueAll.extend()
                    }
                }
            }
        }

        motionLayout?.addTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) = Unit
            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, progress: Float) = Unit
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) = Unit
            override fun onTransitionCompleted(p0: MotionLayout?, id: Int) {
                when (id) {
                    R.id.start -> queueAll.extend()
                    R.id.end -> queueAll.shrink()
                }
            }
        })

        queueAll.setOnClickListener {
            playbackQueue.addItems(adapter.items)
            queueAll.setIconResource(R.drawable.ic_check_24)
            handler.postDelayed(1000) {
                queueAll.setIconResource(R.drawable.ic_playlist_play_24)
            }
        }
    }

    override fun updateItems(items: List<CollectionItem>) {
        val sortedItems = items.sortedWith { a, b ->
            val discDifference = a.discNumber - b.discNumber
            if (discDifference != 0) {
                discDifference
            } else {
                a.trackNumber - b.trackNumber
            }
        }
        adapter.updateItems(sortedItems)
        val item = sortedItems.first()

        var artistString = item.albumArtist ?: item.artist ?: ""
        if (item.year != -1) {
            artistString += " • ${item.year}"
        }
        artist.text = artistString
        title.text = item.album ?: ""
        if (item.artwork != null) {
            api.loadImageIntoView(item, artwork) { image ->
                Palette.from(image).generate { palette ->
                    val swatch = palette?.run {
                        dominantSwatch
                            ?: mutedSwatch
                    }
                    swatch?.run {
                        animateAccentColorChange(rgb, bodyTextColor)
                    }
                }
            }
        } else {
            artwork.setImageResource(R.drawable.ic_fallback_artwork)
            val bgColor = context.getAttrColor(R.attr.colorPrimary)
            val fgColor = context.getAttrColor(R.attr.colorOnPrimary)
            animateAccentColorChange(bgColor, fgColor)
        }
    }

    private fun animateAccentColorChange(bgColor: Int, fgColor: Int) {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        val evaluator = ArgbEvaluator()
        var gradientDrawable: GradientDrawable? = null
        headerBackground?.let {
            val drawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(0, 0))
            drawable.alpha = 0xA0
            it.background = drawable
            gradientDrawable = drawable
        }
        sidebarBackground?.let {
            val drawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(0, 0))
            drawable.alpha = 0xA0
            it.background = drawable
            gradientDrawable = drawable
        }

        val fabInitBg = queueAll.backgroundTintList?.defaultColor ?: 0
        val fabInitFg = queueAll.currentTextColor
        animator.addUpdateListener {
            val f = it.animatedFraction

            val headerBg = evaluator.evaluate(f, 0, bgColor) as Int
            gradientDrawable?.colors = intArrayOf(headerBg, 0)

            val fabBg = evaluator.evaluate(f, fabInitBg, bgColor) as Int
            val fabFg = evaluator.evaluate(f, fabInitFg, fgColor) as Int
            queueAll.background.setTint(fabBg)
            queueAll.setTextColor(fabFg)
            queueAll.iconTint = ColorStateList.valueOf(fabFg)
        }
        animator.duration = 400
        animator.start()
    }

    override fun saveScrollPosition(): Int {
        val layoutManger = recyclerView.layoutManager as LinearLayoutManager
        val scrollPosition = layoutManger.findFirstVisibleItemPosition()

        return when {
            (scrollPosition > 0) -> scrollPosition + 1
            (motionLayout?.progress == 1f) -> 1
            else -> 0
        }
    }

    override fun restoreScrollPosition(position: Int) {
        if (position >= 1) {
            motionLayout?.progress = 1f
            recyclerView.scrollToPosition(position - 1)
            queueAll.shrink()
            Handler().post {
                updateDividerVisibility()
            }
        }
    }

    private fun updateDividerVisibility() {
        if (recyclerView.canScrollVertically(-1)) {
            divider?.visibility = VISIBLE
        } else {
            divider?.visibility = INVISIBLE
        }
    }
}
