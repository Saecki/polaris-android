package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.PlaybackQueue
import agersant.polaris.PolarisApplication
import agersant.polaris.R

import agersant.polaris.api.API
import agersant.polaris.databinding.ViewBrowseAlbumBinding
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*

@SuppressLint("ViewConstructor")
private class BrowseViewAlbum(
    context: Context,
    private val api: API,
    playbackQueue: PlaybackQueue,
) : BrowseViewContent(context) {

    private val adapter: BrowseAdapter
    private val headerBackground: View
    private val artwork: ImageView
    private val artist: TextView
    private val title: TextView

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ViewBrowseAlbumBinding.inflate(inflater, this, true)
        headerBackground = binding.headerBackground
        artwork = binding.albumArtwork
        artist = binding.albumArtist
        title = binding.albumTitle

        val recyclerView = binding.browseRecyclerView
        recyclerView.setHasFixedSize(true)
        val callback: ItemTouchHelper.Callback = BrowseTouchCallback()
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        adapter = BrowseAdapterAlbum(api, playbackQueue)
        recyclerView.adapter = adapter
        recyclerView.setOnScrollChangeListener { v, _, _, _, _ ->
            if (v.canScrollVertically(-1)) {
                binding.headerDivider.visibility = VISIBLE
            } else {
                binding.headerDivider.visibility = INVISIBLE
            }
        }
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
                        val oldBg = 0
                        val newBg = rgb
                        val oldPrimary = title.textColors.defaultColor
                        val newPrimary = titleTextColor
                        val oldSecondary = artist.textColors.defaultColor
                        val newSecondary = bodyTextColor
                        val evaluator = ArgbEvaluator()
                        val animator = ValueAnimator.ofFloat(0f, 1f)
                        animator.addUpdateListener {
                            val f = it.animatedFraction
                            headerBackground.setBackgroundColor(evaluator.evaluate(f, oldBg, newBg) as Int)
                            title.setTextColor(evaluator.evaluate(f, oldPrimary, newPrimary) as Int)
                            artist.setTextColor(evaluator.evaluate(f, oldSecondary, newSecondary) as Int)
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
