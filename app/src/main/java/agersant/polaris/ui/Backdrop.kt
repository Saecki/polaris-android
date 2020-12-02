package agersant.polaris.ui

import agersant.polaris.R
import agersant.polaris.setOnFinished
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.contains
import androidx.customview.widget.Openable
import androidx.navigation.NavController

const val ANIMATION_DURATION: Long = 200L

class BackdropLayout(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    private val wrapContentMeasureSpec = MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

    inner class OverlayView(context: Context) : View(context) {
        init {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            alpha = 0f
            background = ResourcesCompat.getDrawable(resources, R.drawable.content_background, context.theme)

            setOnClickListener { backdropMenu?.close() }
        }
    }

    private var slideAnimator: ViewPropertyAnimator? = null
    private var overlayAnimator: ViewPropertyAnimator? = null
    private var backdropMenu: BackdropMenu? = null
    private val backdropOverlay: OverlayView by lazy { OverlayView(context) }

    fun attachBackdropMenu(backdropMenu: BackdropMenu) {
        this.backdropMenu = backdropMenu
    }

    fun open() {
        if (backdropOverlay !in this) addView(backdropOverlay)

        backdropMenu?.measure(wrapContentMeasureSpec, wrapContentMeasureSpec)
        val targetTranslation = backdropMenu?.measuredHeight?.toFloat() ?: 0f

        this.clearAnimation()
        this.animate()
            .setInterpolator(AccelerateDecelerateInterpolator())
            .translationY(targetTranslation)
            .setDuration(ANIMATION_DURATION)
            .start()

        backdropOverlay.visibility = View.VISIBLE
        backdropOverlay.clearAnimation()
        backdropOverlay.animate()
            .setInterpolator(AccelerateDecelerateInterpolator())
            .alpha(0.5f)
            .setDuration(ANIMATION_DURATION)
            .setOnFinished {}
            .start()
    }

    fun close() {
        this.clearAnimation()
        this.animate()
            .setInterpolator(AccelerateDecelerateInterpolator())
            .translationY(0f)
            .setDuration(ANIMATION_DURATION)
            .start()

        backdropOverlay.clearAnimation()
        backdropOverlay.animate()
            .setInterpolator(AccelerateDecelerateInterpolator())
            .alpha(0f)
            .setDuration(ANIMATION_DURATION)
            .setOnFinished { backdropOverlay.visibility = View.GONE }
            .start()
    }
}

class BackdropMenu(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs), Openable {

    private var isOpen = false
    private var toolbar: Toolbar? = null
    private var toolbarIcon: Drawable? = null

    private var backdropLayoutId: Int? = null
    private val backdropLayout: BackdropLayout? by lazy {
        backdropLayoutId ?: return@lazy null
        val layout: BackdropLayout? = (parent.parent as ViewGroup).findViewById(backdropLayoutId!!) ?: null
        layout?.attachBackdropMenu(this)
        layout
    }

    init {
        alpha = 0f

        val arr = context.obtainStyledAttributes(attrs, R.styleable.BackdropMenu)
        backdropLayoutId = arr.getResourceId(R.styleable.BackdropMenu_backdropLayout, -1)
        arr.recycle()
    }

    fun setUpWith(navController: NavController, toolbar: Toolbar) {
        this.toolbar = toolbar
        navController.addOnDestinationChangedListener { _, _, _ ->
            close()
        }
    }

    override fun isOpen(): Boolean {
        return isOpen
    }

    override fun open() {
        if (isOpen) {
            close()
        } else {
            toolbar ?: throw IllegalStateException("The BackdropMenu has not been set up")

            toolbarIcon = toolbar?.navigationIcon
            toolbar?.setNavigationIcon(R.drawable.baseline_close_24)

            this.visibility = View.VISIBLE
            this.clearAnimation()
            this.animate()
                .setInterpolator(AccelerateDecelerateInterpolator())
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .setOnFinished {}
                .start()
            backdropLayout?.open()

            isOpen = true
        }
    }

    override fun close() {
        if (!isOpen) return
        toolbar ?: throw IllegalStateException("The BackdropMenu has not been set up")

        toolbar?.navigationIcon = toolbarIcon

        this.clearAnimation()
        this.animate()
            .setInterpolator(AccelerateDecelerateInterpolator())
            .alpha(0f)
            .setDuration(ANIMATION_DURATION)
            .setOnFinished { this.visibility = View.GONE }
            .start()
        backdropLayout?.close()

        isOpen = false
    }
}
