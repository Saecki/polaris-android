package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import android.content.Context
import android.view.View

internal abstract class BrowseContent(protected val context: Context) {

    fun interface OnRefreshListener {
        fun onRefresh()
    }

    abstract val root: View

    abstract fun updateItems(items: List<CollectionItem>)

    open fun setOnRefreshListener(listener: OnRefreshListener?) {}

    open fun saveScrollPosition() = 0

    open fun restoreScrollPosition(position: Int) {}
}
