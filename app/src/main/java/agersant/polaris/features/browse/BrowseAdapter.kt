package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

internal abstract class BrowseAdapter : RecyclerView.Adapter<BrowseItemHolder>() {

    inner class DiffCallback : DiffUtil.Callback() {
        var oldItems = items

        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = items.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].path == items[newItemPosition].path
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition] == items[newItemPosition]
        }
    }

    private val diffCallback = DiffCallback()

    var items: List<CollectionItem> = listOf()
        protected set

    override fun onBindViewHolder(holder: BrowseItemHolder, position: Int) {
        holder.bindItem(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    open fun updateItems(items: List<CollectionItem>) {
        diffCallback.oldItems = this.items
        this.items = items
        val diff = DiffUtil.calculateDiff(diffCallback)
        diff.dispatchUpdatesTo(this)
    }
}
