package io.lunarlogic.aircasting.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.AveragingService
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.SensorThreshold
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvc
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvcImpl
import java.util.*
import kotlinx.android.synthetic.main.activity_graph.view.*


abstract class GraphViewMvcImpl: SessionDetailsViewMvcImpl {
    private val graphContainer: GraphContainer
    private val mLoader: ImageView?

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager?
    ): super(inflater, parent, supportFragmentManager) {
        graphContainer = GraphContainer(rootView, context, defaultZoomSpan(), this::onTimeSpanChanged, this::measurementsSample)
        mLoader = rootView?.loader_graph
        showLoader(mLoader)
    }

    abstract fun defaultZoomSpan(): Int?

    open fun measurementsSample(): List<Measurement> {
        return AveragingService(mSessionPresenter?.selectedStream?.measurements).averagedMeasurements() ?: listOf<Measurement>()
    }

    override fun layoutId(): Int {
        return R.layout.activity_graph
    }

    override fun registerListener(listener: SessionDetailsViewMvc.Listener) {
        super.registerListener(listener)
        graphContainer.registerListener(listener)
    }

    override fun unregisterListener(listener: SessionDetailsViewMvc.Listener) {
        super.unregisterListener(listener)
        graphContainer.unregisterListener()
    }

    override fun bindSession(sessionPresenter: SessionPresenter?) {
        super.bindSession(sessionPresenter)
        graphContainer.bindSession(mSessionPresenter)
        hideLoader(mLoader)
    }

    override fun onMeasurementStreamChanged(measurementStream: MeasurementStream) {
        super.onMeasurementStreamChanged(measurementStream)
        graphContainer.refresh(mSessionPresenter)
    }

    override fun onSensorThresholdChanged(sensorThreshold: SensorThreshold) {
        super.onSensorThresholdChanged(sensorThreshold)
        graphContainer.refresh(mSessionPresenter)
    }

    private fun onTimeSpanChanged(timeSpan: ClosedRange<Date>) {
        mSessionPresenter?.visibleTimeSpan = timeSpan
        mStatisticsContainer?.refresh(mSessionPresenter)
    }
}
