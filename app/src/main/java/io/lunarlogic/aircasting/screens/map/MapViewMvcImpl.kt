package io.lunarlogic.aircasting.screens.map

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.libraries.maps.model.*
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.screens.common.BaseViewMvc
import io.lunarlogic.aircasting.screens.common.MeasurementsTableContainer
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session
import kotlinx.android.synthetic.main.activity_map.view.*

class MapViewMvcImpl: BaseObservableViewMvc<MapViewMvc.Listener>, MapViewMvc {
    private val mSessionDateTextView: TextView?
    private val mSessionNameTextView: TextView?
    private val mSessionTagsTextView: TextView?

    private var mSession: Session? = null
    private var mSelectedStream: MeasurementStream? = null

    private val mMeasurementsTableContainer: MeasurementsTableContainer
    private val mMapContainer: MapContainer
    private val mStatisticsContainer: StatisticsContainer

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager?
    ): super() {
        this.rootView = inflater.inflate(R.layout.activity_map, parent, false)

        mSessionDateTextView = this.rootView?.session_date
        mSessionNameTextView = this.rootView?.session_name
        mSessionTagsTextView = this.rootView?.session_tags

        mMeasurementsTableContainer = MeasurementsTableContainer(context, inflater, this.rootView, true, true)
        mMapContainer = MapContainer(rootView, context, supportFragmentManager)
        mStatisticsContainer = StatisticsContainer(this.rootView, context)
    }

    override fun registerListener(listener: MapViewMvc.Listener) {
        super.registerListener(listener)
        mMapContainer.registerListener(listener)
    }

    override fun unregisterListener(listener: MapViewMvc.Listener) {
        super.unregisterListener(listener)
        mMapContainer.unregisterListener()
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

            mStatisticsContainer.addMeasurement(measurement)
        }
    }

    override fun bindSession(session: Session, measurementStream: MeasurementStream?) {
        mSession = session
        mSelectedStream = measurementStream

        bindSessionDetails(session)

        mMapContainer.bindStream(measurementStream)
        val sessionPresenter = SessionPresenter(session, measurementStream)
        mMeasurementsTableContainer.bindSession(sessionPresenter, this::onMeasurementStreamChanged)
        mStatisticsContainer.bindStream(measurementStream)
    }

    override fun centerMap(location: Location) {
        mMapContainer.centerMap(location)
    }

    private fun bindSessionDetails(session: Session) {
        mSessionDateTextView?.text = session.durationString()
        mSessionNameTextView?.text = session.name

        if (session.tags.isEmpty()) {
            mSessionTagsTextView?.visibility = View.GONE
        } else {
            mSessionTagsTextView?.text = session.tagsString()
        }
    }

    private fun onMeasurementStreamChanged(measurementStream: MeasurementStream) {
        mSelectedStream = measurementStream
        mMapContainer.refresh(measurementStream)
        mStatisticsContainer.refresh(measurementStream)
    }
}
