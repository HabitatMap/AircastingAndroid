package io.lunarlogic.aircasting.screens.map

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.forEach
import androidx.fragment.app.FragmentManager
import com.google.android.libraries.maps.model.*
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.screens.common.BaseViewMvc
import io.lunarlogic.aircasting.screens.common.SelectedSensorBorder
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session
import kotlinx.android.synthetic.main.activity_map.view.*

class MapViewMvcImpl: BaseViewMvc, MapViewMvc {
    private val mLayoutInflater: LayoutInflater

    private val mSessionDateTextView: TextView?
    private val mSessionNameTextView: TextView?
    private val mSessionTagsTextView: TextView?

    private val mSessionMeasurementsTable: TableLayout?
    private val mSessionMeasurementHeaders: TableRow?
    private val mMeasurementValues: TableRow?

    private var mSession: Session? = null
    private var mSelectedStream: MeasurementStream? = null

    private val mMapContainer: MapContainer

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager?
    ): super() {
        mLayoutInflater = inflater

        this.rootView = inflater.inflate(R.layout.activity_map, parent, false)

        mSessionDateTextView = this.rootView?.session_date
        mSessionNameTextView = this.rootView?.session_name
        mSessionTagsTextView = this.rootView?.session_tags

        mSessionMeasurementsTable = this.rootView?.measurements_table
        mSessionMeasurementHeaders = this.rootView?.measurement_headers
        mMeasurementValues = this.rootView?.measurement_values

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
        mSelectedStream = measurementStream
        mMapContainer.bindStream(measurementStream)

        bindSessionDetails(session)

        if (session.measurementsCount() > 0) {
            resetMeasurementsView()
            bindMeasurements(session)
            stretchTableLayout(session)
        }
    }

    private fun bindSessionDetails(session: Session) {
        mSession = session
        mSessionDateTextView?.text = session.durationString()
        mSessionNameTextView?.text = session.name
        mSessionTagsTextView?.text = session.tagsString()
    }

    private fun resetMeasurementsView() {
        mSessionMeasurementsTable?.isStretchAllColumns = false
        mSessionMeasurementHeaders?.removeAllViews()
    }

    private fun bindMeasurements(session: Session) {
        session.streamsSortedByDetailedType().forEach { stream ->
            bindStream(stream.detailedType)
            bindLastMeasurement(stream)
        }
    }

    private fun stretchTableLayout(session: Session) {
        if (session.streams.size > 1) {
            mSessionMeasurementsTable?.isStretchAllColumns = true
        }
    }

    private fun bindStream(detailedType: String?) {
        val headerView = mLayoutInflater.inflate(R.layout.measurement_header, null, false)

        val headerTextView = headerView.findViewById<TextView>(R.id.measurement_header)
        headerTextView.text = detailedType

        mSessionMeasurementHeaders?.addView(headerView)
    }

    private fun bindLastMeasurement(stream: MeasurementStream) {
        val measurement = stream.measurements.lastOrNull() ?: return

        val valueView = mLayoutInflater.inflate(R.layout.measurement_value, null, false)
        valueView.background = null

        val circleView = valueView.findViewById<ImageView>(R.id.circle_indicator)
        val valueTextView = valueView.findViewById<TextView>(R.id.measurement_value)

        valueTextView.text = measurement.valueString()

        val color = MeasurementColor.forMap(context, measurement, stream)
        circleView.setColorFilter(color)

        if (stream == mSelectedStream) {
            valueView.background = SelectedSensorBorder(color)
        }

        valueView.setOnClickListener {
            mMeasurementValues?.forEach { it.background = null }
            valueView.background = SelectedSensorBorder(color)

            measurementStreamChanged(stream)
        }

        mMeasurementValues?.addView(valueView)
    }

    private fun measurementStreamChanged(measurementStream: MeasurementStream) {
        mSelectedStream = measurementStream
        mMapContainer.refreshMap(measurementStream)
    }
}
