package io.lunarlogic.aircasting.screens.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.lib.SessionBoundingBox
import io.lunarlogic.aircasting.screens.common.BaseViewMvc
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session
import kotlinx.android.synthetic.main.activity_map.view.*

class MapViewMvcImpl: BaseViewMvc, MapViewMvc, OnMapReadyCallback {
    private val mLayoutInflater: LayoutInflater

    private val mSessionDateTextView: TextView?
    private val mSessionNameTextView: TextView?
    private val mSessionTagsTextView: TextView?

    private val mSessionMeasurementsTable: TableLayout?
    private val mSessionMeasurementHeaders: TableRow?
    private val mMeasurementValues: TableRow?

    private var mSession: Session? = null
    private var mSelectedStream: MeasurementStream? = null

    private lateinit var mMap: GoogleMap

    private val MAX_ZOOM = 20.0f
    private val MIN_ZOOM = 5.0f

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

        val mapFragment = supportFragmentManager?.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap ?: return
        mMap = googleMap
        setZoomPreferences()
        animateCameraToSession()
    }

    private fun animateCameraToSession() {
        val measurements = mSelectedStream?.measurements ?: emptyList()
        val boundingBox = SessionBoundingBox.get(measurements)
        val padding = 100 // meters
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundingBox, padding))
    }

    private fun setZoomPreferences() {
        mMap.setMaxZoomPreference(MAX_ZOOM)
        mMap.setMinZoomPreference(MIN_ZOOM)
    }

    override fun bindSession(session: Session) {
        bindSessionDetails(session)
        mSelectedStream = initialStream(session)

        if (session.measurementsCount() > 0) {
            resetMeasurementsView()
            bindMeasurements(session)
            stretchTableLayout(session)
        }
    }

    private fun initialStream(session: Session): MeasurementStream? {
        return session.streams.firstOrNull()
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
        session.streams.sortedBy { it.detailedType }.forEach { stream ->
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
        val measurement = stream.measurements.lastOrNull()
        val valueView = mLayoutInflater.inflate(R.layout.measurement_value, null, false)

        val circleView = valueView.findViewById<ImageView>(R.id.circle_indicator)
        val valueTextView = valueView.findViewById<TextView>(R.id.measurement_value)

        if (measurement == null) {
            circleView.visibility = View.GONE
        } else {
            valueTextView.text = measurement.valueString()
            val level = measurement.getLevel(stream)
            if (level == null) {
                circleView.visibility = View.GONE
            } else {
                circleView.setColorFilter(MeasurementColor.get(context, level))
            }
        }

        mMeasurementValues?.addView(valueView)
    }
}
