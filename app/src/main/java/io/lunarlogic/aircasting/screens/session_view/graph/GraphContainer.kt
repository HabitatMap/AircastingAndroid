package io.lunarlogic.aircasting.screens.session_view.graph

import android.content.Context
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.DateConverter
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvc
import io.lunarlogic.aircasting.screens.session_view.graph.TargetZoneCombinedChart.TargetZone
import kotlinx.android.synthetic.main.graph.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class GraphContainer: OnChartGestureListener {
    private val mContext: Context
    private var mListener: SessionDetailsViewMvc.Listener? = null

    private var mSessionPresenter: SessionPresenter? = null
    private val mGraph: TargetZoneCombinedChart?
    private val mFromLabel: TextView?
    private val mToLabel: TextView?

    private val mGraphDataGenerator = GraphDataGenerator()

    private val DATE_FORMAT = "HH:mm"
    private val mDefaultZoomSpan: Int?
    private var shouldZoomToDefault = true
    private var mOnTimeSpanChanged: (timeSpan: ClosedRange<Date>) -> Unit
    private var mGetMeasurementsSample: () -> List<Measurement>
    private var mMeasurementsSample: List<Measurement> = listOf()

    constructor(rootView: View?, context: Context, defaultZoomSpan: Int?, onTimeSpanChanged: (timeSpan: ClosedRange<Date>) -> Unit, getMeasurementsSample: () -> List<Measurement>) {
        mContext = context
        mGraph = rootView?.graph
        mFromLabel = rootView?.from_label
        mToLabel = rootView?.to_label
        mDefaultZoomSpan = defaultZoomSpan
        mOnTimeSpanChanged = onTimeSpanChanged
        mGetMeasurementsSample = getMeasurementsSample

        hideGraph()
        setupGraph()
    }

    fun registerListener(listener: SessionDetailsViewMvc.Listener) {
        mListener = listener
    }

    fun unregisterListener() {
        mListener = null
    }

    fun bindSession(sessionPresenter: SessionPresenter?) {
        mSessionPresenter = sessionPresenter
        mMeasurementsSample = mGetMeasurementsSample.invoke()

        drawSession()
        showGraph()
    }

    fun refresh(sessionPresenter: SessionPresenter?) {
        bindSession(sessionPresenter)
    }

    private fun drawSession() {
        val result = generateData()
        val entries = result.entries

        zoom(entries)
        drawData(entries)
        drawMidnightPointLines(result.midnightPoints)
        drawThresholds()
        //setLabels()

        mGraph?.invalidate()
        mGraph?.calculateOffsets()
    }

    private fun setLabels() {
        mGraph?.let { drawLabels(it.lowestVisibleX, it.highestVisibleX) }
    }

    private fun generateData(): GraphDataGenerator.Result {
        return mGraphDataGenerator.generate(mMeasurementsSample)
    }

    private fun drawData(entries: List<Entry>) {
        val combinedData = CombinedData()
        val lineData = buildLineData(entries)
        combinedData.setData(lineData)
        mGraph?.data = combinedData
    }

    private fun zoom(entries: List<Entry>) {
        if (!shouldZoomToDefault) return
        mGraph ?: return

        val first = entries.firstOrNull() ?: return
        val last = entries.lastOrNull() ?: return

        val span = last.x - first.x
        val zoomSpan: Float = mDefaultZoomSpan?.toFloat() ?: span
        val zoom = span / zoomSpan
        val centerX = last.x - Math.min(zoomSpan, span)/2
        val centerY = (last.y - first.y) / 2

        mGraph.zoom(zoom, 1f, centerX, centerY)
        mGraph.moveViewToX(last.x - Math.min(zoomSpan, span))

        val from = Math.max(last.x - zoomSpan, first.x)
        val to = last.x
        drawLabels(from, to)

        shouldZoomToDefault = false
    }

    private fun buildLineData(entries: List<Entry>): LineData {
        val dataSet = LineDataSet(entries, "")
        setupLineAppearance(dataSet)

        return LineData(dataSet)
    }

    private fun drawMidnightPointLines(midnightPoints: List<Float>) {
        val axis = mGraph?.xAxis
        axis?.removeAllLimitLines()
        midnightPoints.forEach { midnightPoint ->
            val line = midnightPointLine(midnightPoint)
            axis?.addLimitLine(line)
        }
        axis?.setDrawLimitLinesBehindData(true)
    }

    private fun drawLabels(from: Float, to: Float) {
        val startDate = mGraphDataGenerator.dateFromFloat(from)
        val endDate = mGraphDataGenerator.dateFromFloat(to)

        mFromLabel?.text = dateString(startDate)
        mToLabel?.text = dateString(endDate)
    }

    private fun dateString(date: Date): String {
        return DateConverter.toDateString(date, TimeZone.getDefault(), DATE_FORMAT)
    }

    private fun midnightPointLine(limit: Float): LimitLine {
        val line = LimitLine(limit, "")
        line.labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
        line.lineColor = ResourcesCompat.getColor(mContext.resources, R.color.aircasting_grey_700, null)
        line.lineWidth = 1f
        line.enableDashedLine(20f, 10f, 0f)
        line.textColor = Color.BLACK
        line.textSize = 12f
        return line
    }

    private fun drawThresholds() {
        val threshold = mSessionPresenter?.selectedSensorThreshold()
        threshold ?: return

        updateValueAxis(threshold.from, threshold.to)

        mGraph?.clearTargetZones()
        MeasurementColor.levels(threshold, mContext).forEach { level ->
            mGraph?.addTargetZone(TargetZone(level.color, level.from.toFloat(), level.to.toFloat()))
        }
    }

    private fun setupLineAppearance(dataSet: LineDataSet) {
        dataSet.color = Color.WHITE
        dataSet.lineWidth = 4.0f
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)
        dataSet.setDrawHighlightIndicators(false)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.cubicIntensity = 0.04f
    }

    private fun updateValueAxis(from: Float, to: Float) {
        mGraph?.axisLeft?.axisMinimum = from
        mGraph?.axisLeft?.axisMaximum = to
        mGraph?.axisRight?.axisMinimum = from
        mGraph?.axisRight?.axisMaximum = to
    }

    private fun setupGraph() {
        mGraph ?: return

        mGraph.setPinchZoom(true)
        mGraph.isScaleYEnabled = false
        mGraph.description = null
        mGraph.legend?.isEnabled = false
        mGraph.axisLeft?.setDrawLabels(false)
        mGraph.axisRight?.setDrawLabels(false)
        mGraph.axisLeft?.setDrawGridLines(false)
        mGraph.axisRight?.setDrawGridLines(false)
        mGraph.xAxis?.setDrawLabels(false)
        mGraph.xAxis?.setDrawGridLines(false)
        mGraph.setDrawGridBackground(false)

        mGraph.onChartGestureListener = this
    }

    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}
    override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
    override fun onChartLongPressed(me: MotionEvent?) {}
    override fun onChartDoubleTapped(me: MotionEvent?) {}
    override fun onChartSingleTapped(me: MotionEvent?) {}
    override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {}
    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
        updateGraphOnGesture()
    }

    override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
        updateGraphOnGesture()
    }

    private fun updateGraphOnGesture() {
        updateLabelsBasedOnVisibleTimeSpan()
        updateVisibleTimeSpan()
    }

    private fun updateLabelsBasedOnVisibleTimeSpan() {
        mGraph ?: return

        GlobalScope.launch(Dispatchers.IO) {
            // we need to wait a bit for drag to finish when there is fling gesture
            // onChartFling does not work properly
            Thread.sleep(500)

            launch(Dispatchers.Main) {
                val from = mGraph.lowestVisibleX
                val to = mGraph.highestVisibleX

                drawLabels(from, to)
            }
        }
    }

    private fun updateVisibleTimeSpan() {
        mGraph ?: return

        val from = mGraph.lowestVisibleX
        val to = mGraph.highestVisibleX
        val timeSpan = mGraphDataGenerator.dateFromFloat(from)..mGraphDataGenerator.dateFromFloat(to)
        mOnTimeSpanChanged.invoke(timeSpan)
    }

    private fun showGraph() {
        mGraph?.visibility = View.VISIBLE
        mFromLabel?.visibility = View.VISIBLE
        mToLabel?.visibility = View.VISIBLE
    }

    private fun hideGraph() {
        mGraph?.visibility = View.GONE
        mFromLabel?.visibility = View.GONE
        mToLabel?.visibility = View.GONE
    }
}
