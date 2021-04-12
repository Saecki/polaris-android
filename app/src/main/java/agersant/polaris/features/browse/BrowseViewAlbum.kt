package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.PlaybackQueue
import agersant.polaris.R
import agersant.polaris.api.API
import agersant.polaris.databinding.ViewBrowseAlbumBinding
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import java.util.*

@SuppressLint("ViewConstructor")
private class BrowseViewAlbum(
    context: Context,
    private val api: API,
    playbackQueue: PlaybackQueue,
) : BrowseViewContent(context) {

    private val adapter: BrowseAdapter
    private val artwork: ImageView
    private val artist: TextView
    private val title: TextView

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ViewBrowseAlbumBinding.inflate(inflater, this, true)
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
            api.loadImageIntoView(item, artwork)
        } else {
            artwork.setImageResource(R.drawable.ic_fallback_artwork)
        }
    }
}
