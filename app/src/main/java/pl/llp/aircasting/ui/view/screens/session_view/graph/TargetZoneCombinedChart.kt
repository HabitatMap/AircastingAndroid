package pl.llp.aircasting.ui.view.screens.session_view.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import com.github.mikephil.charting.charts.CombinedChart


class TargetZoneCombinedChart : CombinedChart {
    private var mYAxisSafeZonePaint: Paint? = null
    private var mTargetZones: MutableList<TargetZone>? = null
    private val points = FloatArray(4)

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun init() {
        super.init()
        mYAxisSafeZonePaint = Paint()
        mYAxisSafeZonePaint?.style = Paint.Style.FILL
        mTargetZones = ArrayList()
//        setViewPortOffsets(4f, 4f, 4f, 4f)
    }

    override fun onDraw(canvas: Canvas) {
        minOffset = 0f
        for (targetZone in mTargetZones!!) {
            // prepare coordinates
            points[1] = targetZone.lowerLimit
            points[3] = targetZone.upperLimit
            mLeftAxisTransformer.pointValuesToPixel(points)

            // draw
            mYAxisSafeZonePaint?.color = targetZone.color
            mYAxisSafeZonePaint?.let {
                canvas.drawRect(
                    mViewPortHandler.contentLeft(), points[1], mViewPortHandler.contentRight(),
                    points[3], it
                )
            }
        }
        super.onDraw(canvas)
    }

    fun addTargetZone(targetZone: TargetZone) {
        mTargetZones!!.add(targetZone)
    }

    val targetZones: List<Any>?
        get() = mTargetZones

    fun clearTargetZones() {
        mTargetZones = ArrayList()
    }

    class TargetZone(val color: Int, val lowerLimit: Float, val upperLimit: Float)
}
