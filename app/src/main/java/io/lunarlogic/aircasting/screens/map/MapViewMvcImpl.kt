package io.lunarlogic.aircasting.screens.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import com.google.android.libraries.maps.model.*
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.lib.SessionBoundingBox
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.screens.common.BaseViewMvc
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session
import kotlinx.android.synthetic.main.activity_map.view.*
import java.util.*

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
    private val DEFAULT_ZOOM = 16f

    private val mMeasurementsLineOptions = PolylineOptions()
        .width(20f)
        .jointType(JointType.ROUND)
        .endCap(RoundCap())
        .startCap(RoundCap())
    private var mMeasurementsLine: Polyline? = null
    private val mMeasurementPoints = ArrayList<LatLng>()
    private val mMeasurementSpans = ArrayList<StyleSpan>()
    private var mLastMeasurementMarker: Marker? = null

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

        val measurements = measurementsWithLocations()
        if (measurements.size > 0) {
            drawSession(measurements)
            animateCameraToSession(measurements)
        } else {
            LocationHelper.start({ centerMap() })
        }
    }

    private fun measurementsWithLocations(): List<Measurement> {
        val measurements = mSelectedStream?.measurements?.filter { it.latitude !== null && it.longitude != null }
        return measurements ?: emptyList()
    }

    private fun setZoomPreferences() {
        mMap.setMaxZoomPreference(MAX_ZOOM)
        mMap.setMinZoomPreference(MIN_ZOOM)
    }

    private fun drawSession(measurements: List<Measurement>) {
        if (mMap == null) return

        var latestPoint: LatLng? = null
        var latestColor: Int? = null

        var i = 0
        for (measurement in measurements) {
            val level = measurement.getLevel(mSelectedStream!!)
            if (level == null) continue // TODO: check this

            latestColor = MeasurementColor.get(context, level)

            if (i > 0) {
                mMeasurementSpans.add(StyleSpan(latestColor))
            }
            latestPoint = LatLng(measurement.latitude!!, measurement.longitude!!)
            mMeasurementPoints.add(latestPoint)
            i += 1
        }
        mMeasurementsLineOptions.addAll(mMeasurementPoints).addAllSpans(mMeasurementSpans)
        mMeasurementsLine = mMap.addPolyline(mMeasurementsLineOptions)

        if (latestPoint != null && latestColor != null) {
            drawLastMeasurementMarker(latestPoint, latestColor)
        }
    }

    private fun drawLastMeasurementMarker(point: LatLng, color: Int) {
        if (mLastMeasurementMarker != null) mLastMeasurementMarker!!.remove()

        mLastMeasurementMarker = mMap.addMarker(
            MarkerOptions()
                .position(point)
                .icon(circleMarkerIcon(color))
        )
    }

    private fun animateCameraToSession(measurements: List<Measurement>) {
        val boundingBox = SessionBoundingBox.get(measurements)
        val padding = 100 // meters
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundingBox, padding))
    }

    private fun centerMap() {
        val location = LocationHelper.lastLocation()
        if (location != null) {
            val position = LatLng(location.latitude, location.longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM))
        }
    }

    override fun bindSession(session: Session) {
        bindSessionDetails(session)

        if (session.measurementsCount() > 0) {
            resetMeasurementsView()
            bindMeasurements(session)
            stretchTableLayout(session)
        }
    }

    override fun bindMeasurementStream(measurementStream: MeasurementStream?) {
        mSelectedStream = measurementStream
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

    // TODO: enhance this
    private fun circleMarkerIcon(color: Int): BitmapDescriptor? {
        val CIRCLE_WIDTH = 30
        val STROKE_WIDTH = 4
        val bitmap = Bitmap.createBitmap(
            CIRCLE_WIDTH + 2 * STROKE_WIDTH,
            CIRCLE_WIDTH + 2 * STROKE_WIDTH,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)
        val strokePaint = Paint()
        strokePaint.style = Paint.Style.STROKE
        strokePaint.color = Color.WHITE
        strokePaint.isAntiAlias = true
        strokePaint.strokeWidth = STROKE_WIDTH.toFloat()
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = color
        paint.isAntiAlias = true
        val radius = CIRCLE_WIDTH / 2
        val padding = 0

        // drawing stroke
        canvas.drawCircle(
            radius + STROKE_WIDTH.toFloat(),
            radius + STROKE_WIDTH.toFloat(),
            radius - padding.toFloat(),
            strokePaint
        )

        // drawing circle filled with proper color
        canvas.drawCircle(
            radius + STROKE_WIDTH.toFloat(),
            radius + STROKE_WIDTH.toFloat(),
            radius - padding.toFloat(),
            paint
        )
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
