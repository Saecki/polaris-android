package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.PlaybackQueue
import agersant.polaris.R

import agersant.polaris.api.API
import agersant.polaris.databinding.ViewBrowseAlbumBinding
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.ItemTouchHelper
import java.util.*

@SuppressLint("ViewConstructor")
private class BrowseViewAlbum(
    context: Context,
    private val api: API,
    playbackQueue: PlaybackQueue,
) : BrowseViewContent(context) {

    private val adapter: BrowseAdapter
    private val motionLayout: MotionLayout?
    private val headerBackground: View?
    private val sidebarBackground: View?
    private val artwork: ImageView
    private val artist: TextView
    private val title: TextView
    private val divider: View?

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ViewBrowseAlbumBinding.inflate(inflater, this, true)
        motionLayout = binding.motionLayout
        headerBackground = binding.headerBackground
        sidebarBackground = binding.sidebarBackground
        artwork = binding.albumArtwork
        artist = binding.albumArtist
        title = binding.albumTitle
        divider = binding.headerDivider

        val recyclerView = binding.browseRecyclerView
        recyclerView.setHasFixedSize(true)

        val callback: ItemTouchHelper.Callback = BrowseTouchCallback()
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        adapter = BrowseAdapterAlbum(api, playbackQueue)
        recyclerView.adapter = adapter

        recyclerView.setOnScrollChangeListener { v, _, _, _, _ ->
            if (v.canScrollVertically(-1)) {
                divider?.visibility = VISIBLE
            } else {
                divider?.visibility = INVISIBLE
            }
        }

        motionLayout?.addTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) = Unit
            override fun onTransitionCompleted(p0: MotionLayout?, id: Int) {
                if (id == R.id.start) {
                    title.textAlignment = TEXT_ALIGNMENT_CENTER
                } else {
                    title.textAlignment = TEXT_ALIGNMENT_TEXT_START
                }
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) = Unit

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, progress: Float) {
                if (progress > 0.8f) {
                    title.textAlignment = TEXT_ALIGNMENT_TEXT_START
                } else {
                    title.textAlignment = TEXT_ALIGNMENT_CENTER
                }
            }
        })
    }

    public override fun setItems(items: ArrayList<out CollectionItem>) {
        items.sortWith { a, b ->
            val discDifference = a.discNumber - b.discNumber
            if (discDifference != 0) {
                discDifference
            } else {
                a.trackNumber - b.trackNumber
            }
        }
        adapter.setItems(items)
        val item = items.first()

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
                        val animator = ValueAnimator.ofArgb(0, rgb)
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
                        animator.addUpdateListener {
                            val bg = it.animatedValue as Int
                            gradientDrawable?.colors = intArrayOf(bg, 0)
                        }
                        animator.duration = 250
                        animator.start()
                    }
                }
            }
        } else {
            artwork.setImageResource(R.drawable.ic_fallback_artwork)
        }
    }
}
