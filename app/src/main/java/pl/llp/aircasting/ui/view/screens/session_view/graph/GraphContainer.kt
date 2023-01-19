package pl.llp.aircasting.ui.view.screens.session_view.graph

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.jobs.MoveViewJob
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.android.synthetic.main.graph.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.ui.view.screens.session_view.graph.TargetZoneCombinedChart.TargetZone
import pl.llp.aircasting.util.MeasurementColor
import pl.llp.aircasting.util.extensions.backToUIThread
import pl.llp.aircasting.util.extensions.runOnIOThread
import pl.llp.aircasting.util.helpers.services.AveragingService
import pl.llp.aircasting.util.isSDKLessThanN
import java.util.*
import kotlin.math.max
import kotlin.math.min

class GraphContainer(
    rootView: View?,
    context: Context,
    defaultZoomSpan: Int?,
    onTimeSpanChanged: (timeSpan: ClosedRange<Date>) -> Unit,
    getMeasurementsSample: () -> List<Measurement>,
    notes: List<Note>?
) : OnChartGestureListener {
    private val MOBILE_SESSION_MEASUREMENT_FREQUENCY = 1000
    private val FIXED_SESSION_MEASUREMENT_FREQUENCY = 60 * 1000
    private var mVisibleEntriesNumber: Int = 60

    private var mContext: Context? = context
    private var mListener: SessionDetailsViewMvc.Listener? = null

    private var mSessionPresenter: SessionPresenter? = null
    private var mGraph: TargetZoneCombinedChart?
    private val mFromLabel: TextView?
    private val mToLabel: TextView?

    private var mGraphDataGenerator: GraphDataGenerator

    private val mDefaultZoomSpan: Int? = defaultZoomSpan
    private var shouldUseDefaultZoom = true
    private var mOnTimeSpanChanged: (timeSpan: ClosedRange<Date>) -> Unit = onTimeSpanChanged
    private var mGetMeasurementsSample: () -> List<Measurement> = getMeasurementsSample
    private var mMeasurementsSample: List<Measurement> = listOf()
    private var mNotes: List<Note>? = notes

    private var mNoteValueRanges: List<ClosedRange<Long>> =
        listOf() // When generating entries for graph I check which entries got their note icon, I keep here "Ranges" of values which I want to react graph click (ChartValueSelectedListener)

    init {
        mGraph = rootView?.graph
        mFromLabel = rootView?.from_label
        mToLabel = rootView?.to_label
        mGraphDataGenerator = GraphDataGenerator(mContext!!)
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
        val graph = mGraph ?: return
        shouldUseDefaultZoom = mSessionPresenter == null
        if (shouldUseDefaultZoom) mVisibleEntriesNumber = mMeasurementsSample.size

//        val onTheRight = graph.highestVisibleX == graph.xChartMax

        mSessionPresenter = sessionPresenter
        mMeasurementsSample = mGetMeasurementsSample.invoke()
        mNotes = mSessionPresenter?.session?.notes
//        if (graph.isFullyZoomedOut) {
//            mVisibleEntriesNumber = mMeasurementsSample.size
//            shouldUseDefaultZoom = onTheRight
//        }

        drawSession()
        if (mMeasurementsSample.isNotEmpty()) showGraph()
    }

    fun refresh(sessionPresenter: SessionPresenter?) {
        bindSession(sessionPresenter)
    }

    fun destroy() {
        mContext = null
        // A "hacky" way to fix a memory leak in MPAndroidChart lib
        // https://github.com/PhilJay/MPAndroidChart/issues/2238
        // it's possible they'll fix it in the future so we must review it
        MoveViewJob.getInstance(null, 0f, 0f, null, null)
        mGraph = null
    }

    private fun drawSession() {
        val result = generateData()
        val entries = result.entries
        mNoteValueRanges = result.noteRanges

        if (shouldUseDefaultZoom)
            zoomToDefaultAndUpdateLabels(entries)
        else
            updateLabels(entries)
        drawData(entries)
        drawMidnightPointLines(result.midnightPoint)
        drawThresholds()

        mGraph?.invalidate()
        mGraph?.calculateOffsets()
    }

    private fun generateData(): GraphDataGenerator.Result {
        return mGraphDataGenerator.generate(
            mMeasurementsSample,
            mNotes,
            visibleMeasurementsSize = mVisibleEntriesNumber,
            averagingFrequency = AveragingService.getAveragingThreshold(
                mMeasurementsSample.firstOrNull(),
                mMeasurementsSample.lastOrNull()
            ),
            isSessionExternal = mSessionPresenter?.isExternal() ?: false
        )
    }

    private fun drawData(entries: List<Entry>) {
        val combinedData = CombinedData()
        val lineData = buildLineData(entries)
        combinedData.setData(lineData)
        mGraph?.data = combinedData
    }

    private fun zoomToDefaultAndUpdateLabels(entries: List<Entry>) {
        mGraph ?: return
        val first = entries.firstOrNull() ?: return
        val last = entries.lastOrNull() ?: return

        val span = last.x - first.x
        val zoomSpan: Float = mDefaultZoomSpan?.toFloat() ?: span
        val zoom = span / zoomSpan
        val centerX = last.x - min(zoomSpan, span) / 2
        val centerY = (last.y - first.y) / 2

        mGraph?.zoom(zoom, 1f, centerX, centerY)
        mGraph?.moveViewToX(last.x - min(zoomSpan, span))

        val from = max(last.x - zoomSpan, first.x)
        val to = last.x
        drawLabels(from, to)

//        shouldUseDefaultZoom = false
    }

    private fun updateLabels(entries: List<Entry>) {
        mGraph ?: return
        val first = entries.firstOrNull() ?: return
        val last = entries.lastOrNull() ?: return

        val span = last.x - first.x
        val zoomSpan: Float = mDefaultZoomSpan?.toFloat() ?: span

        val from = max(last.x - zoomSpan, first.x)
        val to = last.x
        drawLabels(from, to)

//        shouldUseDefaultZoom = false
    }

    private fun buildLineData(entries: List<Entry>): LineData {
        val dataSet = LineDataSet(entries, "")
        setupLineAppearance(dataSet)

        return LineData(dataSet)
    }

    private fun drawMidnightPointLines(midnightPoint: Float) {
        val axis = mGraph?.xAxis
        axis?.removeAllLimitLines()

        val line = midnightPointLine(midnightPoint)
        axis?.addLimitLine(line)
        axis?.setDrawLimitLinesBehindData(true)
    }

    private fun drawLabels(from: Float, to: Float) {
        val startDate = mGraphDataGenerator.dateFromFloat(from)
        val endDate = mGraphDataGenerator.dateFromFloat(to)

        mFromLabel?.text = dateString(startDate)
        mToLabel?.text = dateString(endDate)
    }

    private fun dateString(date: Date): String {
        return GraphDateStringFactory.get(date, mSessionPresenter?.isExternal() ?: false)
    }

    private fun midnightPointLine(limit: Float): LimitLine {
        val line = LimitLine(limit, "")
        line.labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
        mContext?.let { context ->
            line.lineColor =
                ResourcesCompat.getColor(context.resources, R.color.aircasting_grey_700, null)
        }
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
        dataSet.setDrawIcons(true)
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

        mGraph?.setPinchZoom(true)
        mGraph?.isScaleYEnabled = false
        mGraph?.description = null
        mGraph?.legend?.isEnabled = false
        mGraph?.axisLeft?.setDrawLabels(false)
        mGraph?.axisRight?.setDrawLabels(false)
        mGraph?.axisLeft?.setDrawGridLines(false)
        mGraph?.axisRight?.setDrawGridLines(false)
        mGraph?.xAxis?.setDrawLabels(false)
        mGraph?.xAxis?.setDrawGridLines(false)
        mGraph?.setDrawGridBackground(false)
        mGraph?.isDragDecelerationEnabled = false
        mGraph?.setMaxVisibleValueCount(100000) //todo: this allows us to display icons on graph, value may be changed if icons would not display during tests
        if (isSDKLessThanN()) mGraph?.setHardwareAccelerationEnabled(false) // graph wasn't drawn properly for older android versions without this line

        mGraph?.onChartGestureListener = this

        mGraph?.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            // Chart library works the way that we can only click Entries of the graph- we cannot set onClickListener for entry's icon view (note icon in our case)
            // We have a lot (thousands) of entries drawn on the graph for 1-2 hours sessions which means clicking on 1 particular graph entry is not really possible
            // To handle clicking on note somehow, we have to check if user clicked on entry that has attached note icon OR if any graph entry 'close' to entry clicked by user got such icon
            // If yes, we want to launch "noteMarkerClicked" method
            override fun onValueSelected(entry: Entry?, h: Highlight?) {
                try {
                    var noteNumber = -1
                    val tempRanges = mutableListOf<ClosedRange<Long>>()
                    for (range in mNoteValueRanges) {
                        if (entry?.x?.toLong()?.let { range.contains(it) } == true) {
                            tempRanges.add(range)
                        }
                    }
                    when (tempRanges.size) {
                        0 -> {
                            return
                        }
                        1 -> {
                            noteNumber =
                                mNotes?.get(mNoteValueRanges.indexOf(tempRanges.first()))?.number
                                    ?: 0
                        }
                        else -> {
                            // If the clicked Entry is in range of 2 or more "Ranges" then we have to check which Range is the closest one
                            var tempDistance = Long.MAX_VALUE
                            for (range in tempRanges) {
                                val rangeDistance = kotlin.math.abs(
                                    entry?.x?.toLong()
                                        ?.minus(range.start + ((range.endInclusive - range.start) / 2))
                                        ?: Long.MAX_VALUE
                                )
                                if (rangeDistance < tempDistance) {
                                    tempDistance = rangeDistance
                                    noteNumber =
                                        mNotes?.get(mNoteValueRanges.indexOf(range))?.number ?: -1
                                }
                            }
                        }
                    }
                    mListener?.noteMarkerClicked(mSessionPresenter?.session, noteNumber)
                } catch (e: Exception) {
                    Log.e("TAG", "Error while handling note marker click", e)
                }
            }

            override fun onNothingSelected() {
                // do nothing
            }

        })
    }

    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
        updateGraphOnGesture()
        drawSession()
    }

    override fun onChartGestureStart(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
    }

    override fun onChartLongPressed(me: MotionEvent?) {}
    override fun onChartDoubleTapped(me: MotionEvent?) {}
    override fun onChartSingleTapped(me: MotionEvent?) {}
    override fun onChartFling(
        me1: MotionEvent?,
        me2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ) {
    }

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
        updateGraphOnGesture()
    }

    override fun onChartGestureEnd(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
        updateGraphOnGesture()
    }

    private fun updateGraphOnGesture() {
        updateLabelsBasedOnVisibleTimeSpan()
        updateVisibleTimeSpan()
    }

    private fun updateLabelsBasedOnVisibleTimeSpan() {
        mGraph ?: return

        runOnIOThread {
            // we need to wait a bit for drag to finish when there is fling gesture
            // onChartFling does not work properly
            Thread.sleep(500)

            backToUIThread(it) {
                mGraph?.let { graph ->
                    val from = graph.lowestVisibleX
                    val to = graph.highestVisibleX

                    drawLabels(from, to)
                }
            }
        }
    }

    private fun updateVisibleTimeSpan() {
        mGraph?.let { graph ->
            val from = graph.lowestVisibleX
            val to = graph.highestVisibleX
            val timeSpan =
                mGraphDataGenerator.dateFromFloat(from)..mGraphDataGenerator.dateFromFloat(to)

            // TODO: below code is not universal for all types of sensors, we should somehow count "measurement frequency" later on
            mVisibleEntriesNumber = if (!graph.isFullyZoomedOut) {
                val fromDate = Date(from.toLong())
                val toDate = Date(to.toLong())
                val diff =
                    (toDate.time - fromDate.time) / sessionMeasurementsFrequency() // count of measurements between these 2 dates in mobile session, division by 1000 because we need seconds instead of miliseconds
                diff.toInt()
            } else {
                mMeasurementsSample.size
            }

            mOnTimeSpanChanged.invoke(timeSpan)
        }
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

    private fun sessionMeasurementsFrequency(): Int {
        return if (mSessionPresenter?.session?.type == Session.Type.MOBILE)
            MOBILE_SESSION_MEASUREMENT_FREQUENCY
        else
            FIXED_SESSION_MEASUREMENT_FREQUENCY
    }
}
