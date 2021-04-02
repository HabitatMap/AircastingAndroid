package io.lunarlogic.aircasting.screens.session_view.map

import android.content.Context
import android.location.Location
import android.view.View
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.google.android.libraries.maps.*
import com.google.android.libraries.maps.model.*
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.AnimatedLoader
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

    private var mContext: Context?
    private var mListener: SessionDetailsViewMvc.Listener? = null

    private var mMap: GoogleMap? = null
    private val mLocateButton: ImageView?
    private var mMapFragment: SupportMapFragment?
    private var mSupportFragmentManager: FragmentManager?

    private var mSessionPresenter: SessionPresenter? = null
    private var mMeasurements: MutableList<Measurement> = mutableListOf()

    private var mMeasurementsLineOptions: PolylineOptions = defaultPolylineOptions()
    private var mMeasurementsLine: Polyline? = null
    private val mMeasurementPoints = ArrayList<LatLng>()
    private val mMeasurementSpans = ArrayList<StyleSpan>()
    private var mLastMeasurementMarker: Marker? = null
    private var LEVEL_SPANS: Array<StyleSpan>
    private val FALLBACK_SPAN = StyleSpan(R.color.aircasting_grey_700)

    private val status = AtomicInteger(Status.INIT.value)

    enum class Status(val value: Int) {
        INIT(0),
        MAP_LOADED(1),
        SESSION_LOADED(2)
    }

    constructor(rootView: View?, context: Context, supportFragmentManager: FragmentManager?) {
        mContext = context
        this.mSupportFragmentManager = supportFragmentManager

        mMapFragment = SupportMapFragment.newInstance(mapOptions())
        mMapFragment?.let {
            mSupportFragmentManager?.beginTransaction()?.replace(R.id.map, it)?.commit()
        }
        mMapFragment?.getMapAsync(this)
        mMapFragment?.view?.visibility = View.GONE

        mLocateButton = rootView?.locate_button
        mLocateButton?.setOnClickListener {
            locate()
        }
        mLocateButton?.visibility = View.GONE

        LEVEL_SPANS = arrayOf(
            StyleSpan(MeasurementColor.colorForLevel(mContext, Measurement.Level.LOW)),
            StyleSpan(MeasurementColor.colorForLevel(mContext, Measurement.Level.MEDIUM)),
            StyleSpan(MeasurementColor.colorForLevel(mContext, Measurement.Level.HIGH)),
            StyleSpan(MeasurementColor.colorForLevel(mContext, Measurement.Level.EXTREMELY_HIGH)))
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
//        counter = 0
        clearMap()

        mMap?.isBuildingsEnabled = false

        drawSession()
        animateCameraToSession()
        if (mMeasurements.isNotEmpty()) showMap()
    }

    fun bindSession(sessionPresenter: SessionPresenter?, refresh: Boolean = false) {
        mSessionPresenter = sessionPresenter
        if (mMeasurements.isEmpty() || refresh) {
            val measurements = measurementsWithLocations(mSessionPresenter?.selectedStream)
            mMeasurements = if (measurements.isEmpty()) {
                mutableListOf()
            } else {
                measurements as MutableList<Measurement>
            }

        }

        if (mSessionPresenter?.isFixed() == true) {
            drawFixedMeasurement()
        }
        // sometimes onMapReady is invoked earlier than bindStream
        if (status.get() == Status.MAP_LOADED.value) {
            setup()
        }
        if (mMeasurements.isNotEmpty()) status.set(Status.SESSION_LOADED.value)
        mSessionPresenter?.session?.streams?.forEach {
            it.clearMeasurements()
        }
        mSessionPresenter?.selectedStream?.clearMeasurements()
        println("MARYSIA: +mMeasurements.size): "+mMeasurements.size)
        println("MARYSIA: selected stream measurements: "+mSessionPresenter?.selectedStream?.measurements?.size)
        println("MARYSIA: +mSessionPresenter?.session.streams.size): "+mSessionPresenter?.session?.streams?.size)
        println("MARYSIA: +mSessionPresenter?.session?.streams?.first()?.measurements?.size): "+mSessionPresenter?.session?.streams?.first()?.measurements?.size)
    }

    fun destroy() {
        mMap = null
        mContext = null
        mMapFragment?.onDestroy()
        mMapFragment?.let {
            mSupportFragmentManager?.beginTransaction()?.remove(it)?.commitAllowingStateLoss()
        }
        mMapFragment = null
        mSupportFragmentManager = null
        mContext = null
        System.gc()
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
                mMeasurementSpans.add(measurementSpan(measurement))
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

    private fun centerMap(location: Session.Location) {
        val position = LatLng(location.latitude, location.longitude)
        centerMap(position)
    }

    private fun centerMap(position: LatLng) {
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM))
    }

    fun addMobileMeasurement(measurement: Measurement) {
        mMeasurements.add(measurement)
        if (mSessionPresenter?.isRecording() == true) {
            drawMobileMeasurement(measurement)
        }
    }

    private fun drawFixedMeasurement() {
        val colorPoint = measurementColorPoint(mMeasurements.last())
        drawLastMeasurementMarker(colorPoint?.point, colorPoint?.color)
    }

//    var counter = 0

    private fun drawMobileMeasurement(colorPoint: ColorPoint?) {
        if (colorPoint == null) return
//        counter += 1

        mMeasurementPoints.add(colorPoint.point)
//        mMeasurementSpans.add(StyleSpan(colorPoint.color))

        if (mMeasurementsLine == null) {
            mMeasurementsLine = mMap?.addPolyline(mMeasurementsLineOptions)
        }

//        if (counter >= 10 && counter % 5 == 0) {
//            val newMeasurements = mMeasurementPoints.take(mMeasurementPoints.size - 4)
//            mMeasurementPoints.clear()
//            mMeasurementPoints.addAll(newMeasurements)
//
//            val newSpans = mMeasurementSpans.take(mMeasurementSpans.size - 4)
//            mMeasurementSpans.clear()
//            mMeasurementSpans.addAll(newSpans)
//
//            mMeasurementsLine?.setSpans(mMeasurementSpans)
//        }

        mMeasurementsLine?.setPoints(mMeasurementPoints)
//        mMeasurementsLine?.setSpans(mMeasurementSpans)
        drawLastMeasurementMarker(colorPoint.point, colorPoint.color)
    }

    private fun drawMobileMeasurement(measurement: Measurement) {
        val colorPoint = measurementColorPoint(measurement) ?: return

        mMeasurementPoints.add(colorPoint.point)
        mMeasurementSpans.add(measurementSpan(measurement))

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

    private fun measurementSpan(measurement: Measurement) : StyleSpan {
        if (measurement.latitude == null || measurement.longitude == null) return FALLBACK_SPAN
        println("MARYSIA: measurement treshold ${mSessionPresenter?.selectedSensorThreshold()}")
        val treshold = mSessionPresenter?.selectedSensorThreshold() ?: return FALLBACK_SPAN
        val level = measurement.getLevel(treshold)
        println("MARYSIA: measurement level ${level.value}")
        val span = when (level) {
            Measurement.Level.EXTREMELY_LOW -> FALLBACK_SPAN
            Measurement.Level.EXTREMELY_HIGH -> FALLBACK_SPAN
            else -> LEVEL_SPANS[level.value]
        }
        return span
    }

    fun refresh(sessionPresenter: SessionPresenter?) {
        clearMap()
        bindSession(sessionPresenter, true)
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

    private fun showMap() {
        mMapFragment?.view?.visibility = View.VISIBLE
        mLocateButton?.visibility = View.VISIBLE
    }

    private fun mapOptions(): GoogleMapOptions {
        val mapOptions = GoogleMapOptions()
        mapOptions.useViewLifecycleInFragment(true)
        mapOptions.zoomControlsEnabled(true)
        mapOptions.zoomGesturesEnabled(true)

        return mapOptions
    }
}

data class ColorPoint(val point: LatLng, val color: Int)
