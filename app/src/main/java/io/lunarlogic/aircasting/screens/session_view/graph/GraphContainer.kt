package io.lunarlogic.aircasting.screens.session_view.graph

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.fragment.app.FragmentManager
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvc
import io.lunarlogic.aircasting.screens.session_view.graph.TargetZoneCombinedChart.TargetZone
import kotlinx.android.synthetic.main.graph.view.*


class GraphContainer {
    private val mContext: Context
    private var mListener: SessionDetailsViewMvc.Listener? = null

    private var mSessionPresenter: SessionPresenter? = null
    private var mSamples: List<Measurement> = emptyList()
    private val mGraph: TargetZoneCombinedChart?

    private val mGraphDataGenerator = GraphDataGenerator()


    constructor(rootView: View?, context: Context, supportFragmentManager: FragmentManager?) {
        mContext = context
        mGraph = rootView?.graph

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
        mSamples = mSessionPresenter?.selectedStream?.measurements ?: emptyList()

        drawSession()
    }


    fun addMeasurement(measurement: Measurement) {
        // TODO
    }

    fun refresh(sessionPresenter: SessionPresenter?) {
        bindSession(sessionPresenter)
    }

    private fun drawSession() {
        val result = mGraphDataGenerator.generate(mSamples)

        drawData(result.entries)
        drawMidnightPointLines(result.midnightPoints)
        drawThresholds()

        mGraph?.invalidate()
    }

    private fun drawData(entries: List<Entry>) {
        val combinedData = CombinedData()
        val lineData = buildLineData(entries)
        combinedData.setData(lineData)
        mGraph?.data = combinedData
    }

    private fun buildLineData(entries: List<Entry>): LineData {
        val dataSet = LineDataSet(entries, "")
        setupLineAppearance(dataSet)

        return LineData(dataSet)
    }

    private fun drawMidnightPointLines(midnightPoints: List<Float>) {
        midnightPoints.forEach { midnightPoint ->
            val line = midnightPointLine(midnightPoint)
            val axis = mGraph?.xAxis
            axis?.addLimitLine(line)
            axis?.setDrawLimitLinesBehindData(true)
        }
    }

    private fun midnightPointLine(limit: Float): LimitLine {
        val line = LimitLine(limit, "Midnight")
        line.labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
        line.lineColor = Color.BLACK
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
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)
        dataSet.setDrawHighlightIndicators(false)
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
        mGraph.description = null
        mGraph.legend?.isEnabled = false
        mGraph.axisLeft?.setDrawLabels(false)
        mGraph.axisRight?.setDrawLabels(false)
        mGraph.axisLeft?.setDrawGridLines(false)
        mGraph.axisRight?.setDrawGridLines(false)
        mGraph.xAxis?.setDrawLabels(false)
        mGraph.xAxis?.setDrawGridLines(false)
        mGraph.setDrawGridBackground(false)
    }
}
