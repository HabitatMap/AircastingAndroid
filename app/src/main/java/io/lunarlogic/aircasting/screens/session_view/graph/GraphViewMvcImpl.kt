package io.lunarlogic.aircasting.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.SensorThreshold
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvc
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvcImpl


abstract class GraphViewMvcImpl: SessionDetailsViewMvcImpl {
    private val graphContainer: GraphContainer

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager?
    ): super(inflater, parent, supportFragmentManager) {
        graphContainer = GraphContainer(rootView, context, defaultZoomSpan())
    }

    abstract fun defaultZoomSpan(): Int

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

    override fun addMeasurement(measurement: Measurement) {
        super.addMeasurement(measurement)
        graphContainer.addMeasurement(measurement)
    }

    override fun bindSession(sessionPresenter: SessionPresenter?) {
        super.bindSession(sessionPresenter)
        graphContainer.bindSession(mSessionPresenter)
    }

    override fun onMeasurementStreamChanged(measurementStream: MeasurementStream) {
        super.onMeasurementStreamChanged(measurementStream)
        graphContainer.refresh(mSessionPresenter)
    }

    override fun onSensorThresholdChanged(sensorThreshold: SensorThreshold) {
        super.onSensorThresholdChanged(sensorThreshold)
        graphContainer.refresh(mSessionPresenter)
    }
}
