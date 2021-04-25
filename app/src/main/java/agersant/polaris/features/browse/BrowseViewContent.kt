package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import android.content.Context
import android.widget.FrameLayout
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout.OnRefreshListener

internal abstract class BrowseViewContent(context: Context) : FrameLayout(context) {

    abstract fun updateItems(items: List<CollectionItem>)

    open fun setOnRefreshListener(listener: OnRefreshListener?) {}

    open fun saveScrollPosition() = 0

    open fun restoreScrollPosition(position: Int) {}
}
