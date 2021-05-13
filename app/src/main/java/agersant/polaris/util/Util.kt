package agersant.polaris.util

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.Dimension

fun formatTime(time: Int): String {
    val minutes = time / 60
    val seconds = time % 60
    return "%d:%02d".format(minutes, seconds)
}

fun formatDuration(duration: Int): String {
    return if (duration == -1) {
        ""
    } else {
        formatTime(duration)
    }
}

@ColorInt
fun Context.getAttrColor(attr: Int): Int {
    val out = TypedValue()
    theme.resolveAttribute(attr, out, true)
    return out.data
}

val Number.dp: Float
    @Dimension
    get() = (this.toFloat() * Resources.getSystem().displayMetrics.density)

