package eu.mokrzycki.learndigits.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import eu.mokrzycki.learndigits.R

class TopRoundedConstraintLayout : ConstraintLayout {

    private val path = Path()
    private val rect = RectF()
    private val cornerRadii = floatArrayOf(
        resources.getDimensionPixelSize(R.dimen.rounded_corner_radius).toFloat(),
        resources.getDimensionPixelSize(R.dimen.rounded_corner_radius).toFloat(),
        resources.getDimensionPixelSize(R.dimen.rounded_corner_radius).toFloat(),
        resources.getDimensionPixelSize(R.dimen.rounded_corner_radius).toFloat(),
        0f,
        0f,
        0f,
        0f
    )

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onDraw(canvas: Canvas?) {
        canvas?.apply {
            path.reset()
            rect.set(0f, 0f, width.toFloat(), height.toFloat())
            path.addRoundRect(rect, cornerRadii, Path.Direction.CW)
            clipPath(path)
        }
        super.onDraw(canvas)
    }

    init {
        setWillNotDraw(false)
    }
}
