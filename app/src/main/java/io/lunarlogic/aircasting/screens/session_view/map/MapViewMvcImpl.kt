package io.lunarlogic.aircasting.screens.session_view.map

import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.SensorThreshold
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvc
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvcImpl
import kotlinx.android.synthetic.main.activity_map.view.*


abstract class MapViewMvcImpl: SessionDetailsViewMvcImpl {
    private var mMapContainer: MapContainer?
    private val mLoader: ImageView?


    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager?
    ): super(inflater, parent, supportFragmentManager) {
        mMapContainer = MapContainer(rootView, context, supportFragmentManager)
        mLoader = rootView?.loader_map
        showLoader(mLoader)
    }

    override fun layoutId(): Int {
        return R.layout.activity_map
    }

    override fun registerListener(listener: SessionDetailsViewMvc.Listener) {
        super.registerListener(listener)
        mMapContainer?.registerListener(listener)
    }

    override fun unregisterListener(listener: SessionDetailsViewMvc.Listener) {
        super.unregisterListener(listener)
        mMapContainer?.unregisterListener()
    }

    override fun addMeasurement(measurement: Measurement) {
        super.addMeasurement(measurement)
        mMapContainer?.addMobileMeasurement(measurement)
    }

    override fun bindSession(sessionPresenter: SessionPresenter?) {
        super.bindSession(sessionPresenter)
        mMapContainer?.bindSession(mSessionPresenter)
        if (mSessionPresenter?.selectedStream?.measurements?.isNotEmpty() == true) hideLoader(mLoader)
    }

    override fun centerMap(location: Location) {
        mMapContainer?.centerMap(location)
    }

    override fun onMeasurementStreamChanged(measurementStream: MeasurementStream) {
        super.onMeasurementStreamChanged(measurementStream)
        mMapContainer?.refresh(mSessionPresenter)
    }

    override fun onSensorThresholdChanged(sensorThreshold: SensorThreshold) {
        super.onSensorThresholdChanged(sensorThreshold)
        mMapContainer?.refresh(mSessionPresenter)
    }

    override fun onDestroy() {
        mMapContainer?.destroy()
        mMapContainer = null
    }
}
