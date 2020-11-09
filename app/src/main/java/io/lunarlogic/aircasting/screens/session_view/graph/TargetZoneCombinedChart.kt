package io.lunarlogic.aircasting.screens.session_view.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import com.github.mikephil.charting.charts.CombinedChart


class TargetZoneCombinedChart: CombinedChart {
    protected var mYAxisSafeZonePaint: Paint? = null
    private var mTargetZones: MutableList<TargetZoneCombinedChart.TargetZone>? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }

    override fun init() {
        super.init()
        mYAxisSafeZonePaint = Paint()
        mYAxisSafeZonePaint?.setStyle(Paint.Style.FILL)
        mTargetZones = ArrayList()
    }

    override fun onDraw(canvas: Canvas) {
        for (targetZone in mTargetZones!!) {
            // prepare coordinates
            val pts = FloatArray(4)
            pts[1] = targetZone.lowerLimit
            pts[3] = targetZone.upperLimit
            mLeftAxisTransformer.pointValuesToPixel(pts)

            // draw
            mYAxisSafeZonePaint?.setColor(targetZone.color)
            mYAxisSafeZonePaint?.let {
                canvas.drawRect(
                    mViewPortHandler.contentLeft(), pts[1], mViewPortHandler.contentRight(),
                    pts[3], it
                )
            }
        }
        super.onDraw(canvas)
    }

    fun addTargetZone(targetZone: TargetZoneCombinedChart.TargetZone) {
        mTargetZones!!.add(targetZone)
    }

    val targetZones: List<Any>?
        get() = mTargetZones

    fun clearTargetZones() {
        mTargetZones = ArrayList()
    }

    class TargetZone(val color: Int, val lowerLimit: Float, val upperLimit: Float)
}
