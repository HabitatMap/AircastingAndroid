package io.lunarlogic.aircasting.screens.session_view.graph

import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvc
import kotlinx.android.synthetic.main.graph.view.*

class GraphContainer {
    private val mContext: Context
    private var mListener: SessionDetailsViewMvc.Listener? = null

    private var mSessionPresenter: SessionPresenter? = null
    private var mMeasurements: List<Measurement> = emptyList()
    private val mGraph: LineChart?


    constructor(rootView: View?, context: Context, supportFragmentManager: FragmentManager?) {
        mContext = context
        mGraph = rootView?.graph
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
        val entries = mMeasurements.mapIndexed { index, measurement ->  Entry(index.toFloat(), measurement.value.toFloat()) }
        val dataset = LineDataSet(entries, "")
        val lineData = LineData(dataset)

        mGraph?.data = lineData
        mGraph?.invalidate()
    }
}
