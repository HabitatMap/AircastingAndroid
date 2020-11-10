package io.lunarlogic.aircasting.screens.session_view.graph

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.fragment.app.FragmentManager
import com.github.mikephil.charting.data.*
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
    private var mMeasurements: List<Measurement> = emptyList()
    private val mGraph: TargetZoneCombinedChart?


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
        mMeasurements = sessionPresenter?.selectedStream?.measurements ?: emptyList() // TODO

        drawSession()
    }

    fun addMeasurement(measurement: Measurement) {
        // TODO
    }

    fun refresh(sessionPresenter: SessionPresenter?) {
        bindSession(sessionPresenter)
    }

    private fun drawSession() {
        updateData()
        updateThresholds()
        mGraph?.invalidate()
    }

    private fun updateData() {
        val combinedData = CombinedData()
        combinedData.setData(buildEntries())
        mGraph?.data = combinedData
    }

    private fun updateThresholds() {
        val threshold = mSessionPresenter?.selectedSensorThreshold()
        threshold ?: return

        updateValueAxis(threshold.from, threshold.to)

        mGraph?.clearTargetZones()
        MeasurementColor.levels(threshold, mContext).forEach { level ->
            mGraph?.addTargetZone(TargetZone(level.color, level.from.toFloat(), level.to.toFloat()))
        }
    }

    private fun buildEntries(): LineData {
        val entries = mMeasurements.mapIndexed { index, measurement ->  Entry(
            index.toFloat(),
            measurement.value.toFloat()
        ) }

        val dataSet = LineDataSet(entries, "")
        setupLineAppearance(dataSet)

        return LineData(dataSet)
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
