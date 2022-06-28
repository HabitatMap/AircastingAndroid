package pl.llp.aircasting.ui.view.screens.session_view.map

import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvcImpl
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
        if (mSessionPresenter?.selectedStream?.measurements?.isNotEmpty() == true) {
            hideLoader(mLoader)
        }
        mMapContainer?.bindSession(mSessionPresenter)

    }

    override fun centerMap(location: Location) {
        mMapContainer?.centerMap(location)
    }

    override fun onMeasurementStreamChanged(measurementStream: MeasurementStream) {
        super.onMeasurementStreamChanged(measurementStream)
        mMapContainer?.refresh(mSessionPresenter)
    }

    override fun addNote(note: Note) {
        super.onNoteAdded(note)
        mMapContainer?.refresh(mSessionPresenter)
    }

    override fun deleteNote(note: Note) {
        super.onNoteDeleted(note)
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
