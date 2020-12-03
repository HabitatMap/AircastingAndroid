package io.lunarlogic.aircasting.screens.session_view.map

import android.content.Context
import android.location.Location
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.*
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.BitmapHelper
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.lib.SessionBoundingBox
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvc
import kotlinx.android.synthetic.main.activity_map.view.*
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicInteger

class MapContainer: OnMapReadyCallback {
    private val DEFAULT_ZOOM = 16f

    private val mContext: Context
    private var mListener: SessionDetailsViewMvc.Listener? = null

    private var mMap: GoogleMap? = null
    private val mLocateButton: ImageView?

    private var mSessionPresenter: SessionPresenter? = null
    private var mMeasurements: List<Measurement> = emptyList()

    private var mMeasurementsLineOptions: PolylineOptions = defaultPolylineOptions()
    private var mMeasurementsLine: Polyline? = null
    private val mMeasurementPoints = ArrayList<LatLng>()
    private val mMeasurementSpans = ArrayList<StyleSpan>()
    private var mLastMeasurementMarker: Marker? = null

    private val status = AtomicInteger(Status.INIT.value)

    enum class Status(val value: Int) {
        INIT(0),
        MAP_LOADED(1),
        SESSION_LOADED(2)
    }

    constructor(rootView: View?, context: Context, supportFragmentManager: FragmentManager?) {
        mContext = context

        val mapFragment = supportFragmentManager?.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        mLocateButton = rootView?.locate_button
        mLocateButton?.setOnClickListener {
            locate()
        }
    }

    fun registerListener(listener: SessionDetailsViewMvc.Listener) {
        mListener = listener
    }

    fun unregisterListener() {
        mListener = null
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap ?: return
        mMap = googleMap

        // sometimes onMapReady is invoked earlier than bindStream
        if (status.get() == Status.SESSION_LOADED.value) {
            setup()
        }
        status.set(Status.MAP_LOADED.value)
    }

    fun setup() {
        clearMap()

        mMap?.isBuildingsEnabled = false

        drawSession()
        animateCameraToSession()
    }

    fun bindSession(sessionPresenter: SessionPresenter?) {
        mSessionPresenter = sessionPresenter
        mMeasurements = measurementsWithLocations(mSessionPresenter?.selectedStream)

        if (mSessionPresenter?.isFixed() == true) {
            drawFixedMeasurement()
        }
        // sometimes onMapReady is invoked earlier than bindStream
        if (status.get() == Status.MAP_LOADED.value) {
            setup()
        }
        status.set(Status.SESSION_LOADED.value)
    }

    private fun measurementsWithLocations(stream: MeasurementStream?): List<Measurement> {
        val measurements = stream?.measurements?.filter { it.latitude !== null && it.longitude != null }
        return measurements ?: emptyList()
    }

    private fun drawSession() {
        if (mMap == null) return
        if (mMeasurements.isEmpty()) return

        var latestPoint: LatLng? = null
        var latestColor: Int? = null

        var i = 0
        for (measurement in mMeasurements) {
            latestColor = MeasurementColor.forMap(mContext, measurement, mSessionPresenter?.selectedSensorThreshold())

            if (i > 0) {
                mMeasurementSpans.add(StyleSpan(latestColor))
            }
            latestPoint = LatLng(measurement.latitude!!, measurement.longitude!!)
            mMeasurementPoints.add(latestPoint)
            i += 1
        }
        mMeasurementsLineOptions.addAll(mMeasurementPoints).addAllSpans(mMeasurementSpans)
        mMeasurementsLine = mMap?.addPolyline(mMeasurementsLineOptions)

        if (latestPoint != null && latestColor != null) {
            drawLastMeasurementMarker(latestPoint, latestColor)
        }
    }

    private fun drawLastMeasurementMarker(point: LatLng?, color: Int?) {
        if (point == null || color == null) return
        if (mLastMeasurementMarker != null) mLastMeasurementMarker!!.remove()

        val icon = BitmapHelper.bitmapFromVector(mContext, R.drawable.ic_dot_20, color)
        mLastMeasurementMarker = mMap?.addMarker(
            MarkerOptions()
                .position(point)
                .icon(icon)
        )
    }

    private fun animateCameraToSession() {
        if (mSessionPresenter?.isFixed() == true) {
            animateCameraToFixedSession()
        } else {
            animateCameraToMobileSession()
        }
    }

    private fun animateCameraToMobileSession() {
        if (mMeasurements.isEmpty()) return

        val boundingBox = SessionBoundingBox.get(mMeasurements)
        val padding = 100 // meters
        mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(boundingBox, padding))
    }

    private fun animateCameraToFixedSession() {
        val session = mSessionPresenter?.session
        val location = session?.location

        location ?: return

        centerMap(location)
    }

    fun centerMap(location: Location) {
        val position = LatLng(location.latitude, location.longitude)
        centerMap(position)
    }

    fun centerMap(location: Session.Location) {
        val position = LatLng(location.latitude, location.longitude)
        centerMap(position)
    }

    fun centerMap(position: LatLng) {
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM))
    }

    fun addMeasurement(measurement: Measurement) {
        if (mSessionPresenter?.isRecording() == true) {
            drawMobileMeasurement(measurementColorPoint(measurement))
        }
    }

    private fun drawFixedMeasurement() {
        val colorPoint = measurementColorPoint(mMeasurements.last())
        drawLastMeasurementMarker(colorPoint?.point, colorPoint?.color)
    }

    private fun drawMobileMeasurement(colorPoint: ColorPoint?) {
        if (colorPoint == null) return

        mMeasurementPoints.add(colorPoint.point)
        mMeasurementSpans.add(StyleSpan(colorPoint.color))

        if (mMeasurementsLine == null) {
            mMeasurementsLine = mMap?.addPolyline(mMeasurementsLineOptions)
        }

        mMeasurementsLine?.setPoints(mMeasurementPoints)
        mMeasurementsLine?.setSpans(mMeasurementSpans)

        drawLastMeasurementMarker(colorPoint.point, colorPoint.color)
    }

    private fun measurementColorPoint(measurement: Measurement) : ColorPoint? {
        if (measurement.latitude == null || measurement.longitude == null) return null

        val point = LatLng(measurement.latitude, measurement.longitude)
        val color = MeasurementColor.forMap(mContext, measurement, mSessionPresenter?.selectedSensorThreshold())

        return ColorPoint(point, color)
    }

    fun refresh(sessionPresenter: SessionPresenter?) {
        clearMap()
        bindSession(sessionPresenter)
        drawSession()
    }

    private fun locate() {
        mListener?.locateRequested()
    }

    private fun clearMap() {
        mMap?.clear()
        mMeasurementPoints.clear()
        mMeasurementSpans.clear()
        mMeasurementsLine = null
        mMeasurementsLineOptions = defaultPolylineOptions()
    }

    private fun defaultPolylineOptions(): PolylineOptions {
        return PolylineOptions()
            .width(20f)
            .jointType(JointType.ROUND)
            .endCap(RoundCap())
            .startCap(RoundCap())
    }
}

data class ColorPoint(val point: LatLng, val color: Int)
