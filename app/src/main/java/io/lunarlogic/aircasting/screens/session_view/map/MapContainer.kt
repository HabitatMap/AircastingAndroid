package io.lunarlogic.aircasting.screens.session_view.map

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.view.View
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.google.android.libraries.maps.*
import com.google.android.libraries.maps.model.*
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.BitmapHelper
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.lib.SessionBoundingBox
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.Note
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvc
import kotlinx.android.synthetic.main.activity_map.view.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class MapContainer: OnMapReadyCallback {
    private val DEFAULT_ZOOM = 16f

    private var mContext: Context?
    private var mListener: SessionDetailsViewMvc.Listener? = null

    private var mMap: GoogleMap? = null
    private val mLocateButton: ImageView?
    private var mMapFragment: SupportMapFragment?
    private var mSupportFragmentManager: FragmentManager?
    private var mMarkers: HashMap<String?, Int?>? = hashMapOf()

    private var mSessionPresenter: SessionPresenter? = null
    private var mMeasurements: List<Measurement> = emptyList()
    private var mNotes: List<Note> = emptyList()

    private var mMeasurementsLineOptions: PolylineOptions = defaultPolylineOptions()
    private var mMeasurementsLine: Polyline? = null
    private val mMeasurementPoints = ArrayList<LatLng>()
    private var mLastMeasurementMarker: Marker? = null

    private var mMapGrid: MapGrid? = null

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

        mMap?.setOnCameraIdleListener {
            drawHeatMap()
        }
        if (mMeasurements.isNotEmpty()) showMap()
    }

    fun bindSession(sessionPresenter: SessionPresenter?) {
        mSessionPresenter = sessionPresenter
        mMeasurements = measurementsWithLocations(mSessionPresenter?.selectedStream)
        mNotes = mSessionPresenter?.session?.notes!!

        if (mSessionPresenter?.isFixed() == true) {
            drawFixedMeasurement()
        }
        // sometimes onMapReady is invoked earlier than bindStream
        if (status.get() == Status.MAP_LOADED.value) {
            setup()
        }
        if (mMeasurements.isNotEmpty()) status.set(Status.SESSION_LOADED.value)
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
        mMarkers = null
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
            latestPoint = LatLng(measurement.latitude!!, measurement.longitude!!)
            mMeasurementPoints.add(latestPoint)
            i += 1
        }
        mMeasurementsLineOptions.addAll(mMeasurementPoints)
        mMeasurementsLine = mMap?.addPolyline(mMeasurementsLineOptions)

        if (latestPoint != null && latestColor != null) {
            drawLastMeasurementMarker(latestPoint, latestColor)
        }

        drawNotes()
    }

    private fun drawNotes() {
        for (note in mNotes) {
            drawNoteMarker(note)
        }
    }

    private fun drawHeatMap() {
        if (mSessionPresenter?.isFixed() == true) return

        val mapWidth = mMapFragment?.view?.width ?: 0
        val mapHeight = mMapFragment?.view?.height ?: 0

        mMap?.let { map ->
            val sensorThreshold = mSessionPresenter?.selectedSensorThreshold()
            sensorThreshold?.let { sensorThreshold ->
                if (mMapGrid != null) {
                    mMapGrid?.remove()
                    mMapGrid = null
                }
                mMapGrid = MapGrid(mContext, map, sensorThreshold, mapWidth, mapHeight )
                mMapGrid?.drawHeatMap(mMeasurements)
            }
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

    private fun drawNoteMarker(note: Note) {
        if (note.latitude == null || note.longitude == null) return

        val icon = BitmapHelper.bitmapFromVector(mContext, R.drawable.ic_note_icon)
        val marker = mMap?.addMarker(MarkerOptions()
            .position(LatLng(note.latitude, note.longitude))
            .icon(icon))
        mMarkers?.set(marker?.id, mSessionPresenter?.session?.notes?.get(note.number)?.number)  // todo: hmmm, i should probably search in 'notes' field with number equal note.number, not by the notes index
        mMap?.setOnMarkerClickListener { marker ->
            val noteNumber = mMarkers?.get(marker.id)
            if (noteNumber != null) {
                mListener?.noteMarkerClicked(mSessionPresenter?.session, noteNumber)
            }
            false
        }
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
        if (mSessionPresenter?.isRecording() == true) {
            drawMobileMeasurement(measurementColorPoint(measurement))
            mMapGrid?.addMeasurement(measurement)
        }
    }

    private fun drawFixedMeasurement() {
        val colorPoint = measurementColorPoint(mMeasurements.last())
        drawLastMeasurementMarker(colorPoint?.point, colorPoint?.color)
    }

    private fun drawMobileMeasurement(colorPoint: ColorPoint?) {
        if (colorPoint == null) return

        mMeasurementPoints.add(colorPoint.point)

        if (mMeasurementsLine == null) {
            mMeasurementsLine = mMap?.addPolyline(mMeasurementsLineOptions)
        }

        mMeasurementsLine?.setPoints(mMeasurementPoints)
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
        drawHeatMap()
        drawSession()
    }

    private fun locate() {
        mListener?.locateRequested()
    }

    private fun clearMap() {
        mMap?.clear()
        mMeasurementPoints.clear()
        mMeasurementsLine = null
        mMeasurementsLineOptions = defaultPolylineOptions()
    }

    private fun defaultPolylineOptions(): PolylineOptions {
        val lineColor = mContext?.let { context ->
            ResourcesCompat.getColor(context.resources, R.color.aircasting_blue_400, null)
        }

        return PolylineOptions()
            .width(10f)
            .color(lineColor ?: Color.BLUE)
            .jointType(JointType.ROUND)
            .endCap(RoundCap())
            .startCap(RoundCap())
            .zIndex(1000f)
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
