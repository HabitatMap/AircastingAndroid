package io.lunarlogic.aircasting.screens.map

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.libraries.maps.model.*
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.screens.common.BaseViewMvc
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session
import kotlinx.android.synthetic.main.activity_map.view.*

class MapViewMvcImpl: BaseViewMvc, MapViewMvc {
    private val mSessionDateTextView: TextView?
    private val mSessionNameTextView: TextView?
    private val mSessionTagsTextView: TextView?

    private var mSession: Session? = null
    private var mSelectedStream: MeasurementStream? = null

    private val mTableController: TableContainer
    private val mMapContainer: MapContainer

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager?
    ): super() {
        this.rootView = inflater.inflate(R.layout.activity_map, parent, false)

        mSessionDateTextView = this.rootView?.session_date
        mSessionNameTextView = this.rootView?.session_name
        mSessionTagsTextView = this.rootView?.session_tags

        mTableController = TableContainer(context, inflater, this.rootView)
        mMapContainer = MapContainer(context, supportFragmentManager)
    }

    override fun addMeasurement(measurement: Measurement) {
        if (measurement.latitude == null || measurement.longitude == null) return

        val point = LatLng(measurement.latitude, measurement.longitude)
        val color = MeasurementColor.forMap(context, measurement, mSelectedStream!!)

        mSession?.let {
            if (it.isFixed()) {
                mMapContainer.drawFixedMeasurement(point, color)
            } else if (it.isRecording()) {
                mMapContainer.drawMobileMeasurement(point, color)
            }
        }
    }

    override fun bindSession(session: Session, measurementStream: MeasurementStream?) {
        mSession = session
        mSelectedStream = measurementStream

        bindSessionDetails(session)

        mMapContainer.bindStream(measurementStream)
        mTableController.bindSession(session, mSelectedStream, this::onMeasurementStreamChanged)
    }

    private fun bindSessionDetails(session: Session) {
        mSessionDateTextView?.text = session.durationString()
        mSessionNameTextView?.text = session.name
        mSessionTagsTextView?.text = session.tagsString()
    }

    private fun onMeasurementStreamChanged(measurementStream: MeasurementStream) {
        mSelectedStream = measurementStream
        mMapContainer.refreshMap(measurementStream)
    }
}
