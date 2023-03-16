package pl.llp.aircasting.ui.view.screens.session_view.map

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_map.view.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.util.*
import pl.llp.aircasting.util.extensions.setMapType
import java.util.concurrent.atomic.AtomicInteger

class MapContainer(rootView: View?, context: Context, supportFragmentManager: FragmentManager?) :
    OnMapReadyCallback {
    private val DEFAULT_ZOOM = 16f
    private var currentZoom: Float? = null

    private var mContext: Context? = context
    private var mListener: SessionDetailsViewMvc.Listener? = null
    private var isCenteredToLastLocation = false

    private var mMap: GoogleMap? = null
    private val mLocateButton: ImageView?
    private var mMapFragment: SupportMapFragment?
    private var mSupportFragmentManager: FragmentManager? = supportFragmentManager
    private var mMarkers: HashMap<String?, Int?>? = hashMapOf()

    private var mSessionPresenter: SessionPresenter? = null
    private var mMeasurements: List<Measurement> = emptyList()
    private var mNotes: List<Note> = emptyList()

    private var mMeasurementsLineOptions: PolylineOptions = defaultPolylineOptions()
    private var mMeasurementsLine: Polyline? = null
    private val mMeasurementPoints = ArrayList<LatLng>()
    private var mLastMeasurementMarker: Marker? = null

    private var mAircastingHeatmap: AircastingHeatmap? = null

    private val status = AtomicInteger(Status.INIT.value)
    private var mSettings: Settings
    private var mApplication: AircastingApplication

    enum class Status(val value: Int) {
        INIT(0),
        MAP_LOADED(1),
        SESSION_LOADED(2)
    }

    init {
        mMapFragment = SupportMapFragment.newInstance(mapOptions())

        mApplication = context.applicationContext as AircastingApplication
        mSettings = mApplication.settings

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

    fun setup() {
        clearMap()

        mMap?.isBuildingsEnabled = true

        drawSession()
        animateCameraToSession()

        mMap?.setOnCameraIdleListener {
            drawHeatMap()
            if (!isCenteredToLastLocation) setCurrentZoom() // we want to center the map over last user's location only when user enters the map

        }
        if (mMeasurements.isNotEmpty()) showMap()
    }

    private fun setCurrentZoom() {
        if (mMeasurements.isEmpty()) return
        currentZoom = mMap?.cameraPosition?.zoom

        if (mSessionPresenter?.isRecording() == true) {
            val lastLat = mMeasurements.last().latitude
            val lastLong = mMeasurements.last().longitude
            centerMap(LatLng(lastLat!!, lastLong!!), currentZoom!!)

        }

        isCenteredToLastLocation = true
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
        val measurements =
            stream?.measurements?.filter { it.latitude !== null && it.longitude != null }
        return measurements ?: emptyList()
    }

    private fun drawSession() {
        if (mMap == null) return
        if (mMeasurements.isEmpty()) return

        var latestPoint: LatLng? = null
        var latestColor: Int? = null

        var i = 0
        for (measurement in mMeasurements) {
            latestColor = MeasurementColor.forMap(
                mContext,
                measurement,
                mSessionPresenter?.selectedSensorThreshold()
            )
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
            sensorThreshold?.let {
                if (mAircastingHeatmap != null) {
                    mAircastingHeatmap?.remove()
                    mAircastingHeatmap = null
                }
                mAircastingHeatmap =
                    AircastingHeatmap(mContext, map, it, mapWidth, mapHeight)
                mAircastingHeatmap?.drawHeatMap(mMeasurements)
            }
        }

    }


    private fun drawLastMeasurementMarker(point: LatLng?, color: Int?) {
        if (!shouldDrawLastMeasurement()) return
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
        val marker = mMap?.addMarker(
            MarkerOptions()
                .position(LatLng(note.latitude, note.longitude))
                .icon(icon)
        )
        marker?.zIndex =
            Float.MAX_VALUE // We set Z-index so the note marker are first to be 'clicked' when user press map
        mMarkers?.set(marker?.id, note.number)
        mMap?.setOnMarkerClickListener {
            val noteNumber = mMarkers?.get(it.id)
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

        mMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(boundingBox, padding))

        val zoomLevel = mMap?.cameraPosition?.zoom ?: return
        if (zoomLevel > DEFAULT_ZOOM)
            centerMap(mMap?.cameraPosition?.target)
    }

    private fun animateCameraToFixedSession() {
        if (mMeasurements.isEmpty()) return
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

    private fun centerMap(position: LatLng?, zoom: Float = DEFAULT_ZOOM) {
        position ?: return

        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom))
    }

    fun addMobileMeasurement(measurement: Measurement) {
        if (mSessionPresenter?.isRecording() == true) {
            drawMobileMeasurement(measurementColorPoint(measurement))
            mAircastingHeatmap?.addMeasurement(measurement)
        }
    }

    private fun drawFixedMeasurement() {
        if (mMeasurements.isEmpty()) return
        val colorPoint = measurementColorPoint(mMeasurements.last())
        drawLastMeasurementMarker(colorPoint?.point, colorPoint?.color)
    }

    private fun drawMobileMeasurement(colorPoint: ColorPoint?) {
        if (colorPoint == null) return

        mMeasurementPoints.add(colorPoint.point)

        if (mMeasurementsLine == null) {
            mMeasurementsLine = mMap?.addPolyline(mMeasurementsLineOptions)
        }

        mMeasurementsLine?.points = mMeasurementPoints
        drawLastMeasurementMarker(colorPoint.point, colorPoint.color)
    }

    private fun measurementColorPoint(measurement: Measurement): ColorPoint? {
        if (measurement.latitude == null || measurement.longitude == null) return null

        val point = LatLng(measurement.latitude, measurement.longitude)
        val color = MeasurementColor.forMap(
            mContext,
            measurement,
            mSessionPresenter?.selectedSensorThreshold()
        )

        return ColorPoint(point, color)
    }

    fun refresh(sessionPresenter: SessionPresenter?) {
        clearMap()
        bindSession(sessionPresenter)
        drawHeatMap()
        drawSession()
    }

    private fun locate() {
        if (mSessionPresenter?.session?.location?.latitude != 0.0 || mSessionPresenter?.session?.location?.longitude != 0.0) {
            if (mSessionPresenter?.isMobileActive() == true) mListener?.locateRequested() else
                mSessionPresenter?.session?.location?.let { centerMap(it) }
        } else {
            if (mMeasurements.isNotEmpty()) mMeasurements.apply {
                centerMap(
                    Session.Location(
                        first().latitude!!,
                        first().longitude!!
                    )
                )
            }
        }
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
        mapOptions.zoomControlsEnabled(false)
        mapOptions.zoomGesturesEnabled(true)
        mapOptions.mapToolbarEnabled(false)

        return mapOptions
    }

    private fun setCustomCompassLocation() {
        mMapFragment?.view?.let { mapView ->
            mapView.findViewWithTag<View>("GoogleMapMyLocationButton").parent.let { parent ->
                val vg: ViewGroup = parent as ViewGroup
                vg.post {
                    val mapCompass: View = parent.getChildAt(4)
                    val rlp = RelativeLayout.LayoutParams(mapCompass.height, mapCompass.height)
                    rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0)
                    rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                    rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                    rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)

                    val topMargin = (5 * Resources.getSystem().displayMetrics.density).toInt()
                    val rightMargin = (5 * Resources.getSystem().displayMetrics.density).toInt()
                    rlp.setMargins(0, topMargin, rightMargin, 0)
                    mapCompass.layoutParams = rlp
                }
            }
        }
    }

    private fun shouldDrawLastMeasurement(): Boolean {
        return mSessionPresenter?.session?.tab != SessionsTab.MOBILE_DORMANT
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mContext?.let { mMap?.setMapType(mSettings, it) }

        // sometimes onMapReady is invoked earlier than bindStream
        if (status.get() == Status.SESSION_LOADED.value) {
            setup()
        }
        status.set(Status.MAP_LOADED.value)

        setCustomCompassLocation()
    }
}

data class ColorPoint(val point: LatLng, val color: Int)
