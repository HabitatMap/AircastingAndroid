package io.lunarlogic.aircasting.screens.session_view.map

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.alpha
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
import io.lunarlogic.aircasting.models.SensorThreshold
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvc
import kotlinx.android.synthetic.main.activity_map.view.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.HashMap

class MapContainer: OnMapReadyCallback {
    private val DEFAULT_ZOOM = 16f
    private var LEVEL_SPANS: Array<StyleSpan>
    private val FALLBACK_SPAN: StyleSpan

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
//    private val mMeasurementSpans = ArrayList<StyleSpan>()
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

        LEVEL_SPANS = arrayOf(
            StyleSpan(MeasurementColor.colorForLevel(mContext, Measurement.Level.LOW)),
            StyleSpan(MeasurementColor.colorForLevel(mContext, Measurement.Level.MEDIUM)),
            StyleSpan(MeasurementColor.colorForLevel(mContext, Measurement.Level.HIGH)),
            StyleSpan(MeasurementColor.colorForLevel(mContext, Measurement.Level.EXTREMELY_HIGH)))
        FALLBACK_SPAN = StyleSpan(R.color.aircasting_grey_700)
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
            println("MARYSIA: visibleRegion camera idle")
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

//            if (i > 0) {
//                mMeasurementSpans.add(measurementSpan(measurement))
//            }
            latestPoint = LatLng(measurement.latitude!!, measurement.longitude!!)
            mMeasurementPoints.add(latestPoint)
            i += 1
        }
        mMeasurementsLineOptions.addAll(mMeasurementPoints)//.addAllSpans(mMeasurementSpans)
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
        val mapWidth = mMapFragment?.view?.width ?: 0
        val mapHeight = mMapFragment?.view?.height ?: 0


        mMap?.let {map ->
            val sensorThreshold = mSessionPresenter?.selectedSensorThreshold()
            sensorThreshold?.let { sensorThreshold ->
                if (mMapGrid != null) {
                    mMapGrid?.remove()
                    mMapGrid = null
                }
                mMapGrid = MapGrid(mContext, map, sensorThreshold, mapWidth, mapHeight )
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
//        mMeasurementSpans.add(StyleSpan(colorPoint.color))

        if (mMeasurementsLine == null) {
            mMeasurementsLine = mMap?.addPolyline(mMeasurementsLineOptions)
        }

        mMeasurementsLine?.setPoints(mMeasurementPoints)
//        mMeasurementsLine?.setSpans(mMeasurementSpans)
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
        val threshold = mSessionPresenter?.selectedSensorThreshold() ?: return FALLBACK_SPAN
        return when (val level = measurement.getLevel(threshold)) {
            Measurement.Level.EXTREMELY_LOW -> FALLBACK_SPAN
            Measurement.Level.EXTREMELY_HIGH -> FALLBACK_SPAN
            else -> LEVEL_SPANS[level.value]
        }
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
//        mMeasurementSpans.clear()
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

class GridSquare {
    private var mGridMap: MapGrid
    private var mBounds: LatLngBounds
    private var sum: Double = 0.0
    private var number: Int = 0
    private var averagedValue: Double = 0.0
    private var level: Measurement.Level? = null
    var mPolygonOptions: PolygonOptions
    private var mPolygon: Polygon? = null
    var newColor: Boolean = false
    val mSouthWestLatLng: LatLng
    val mSouthEastLatLng: LatLng
    val mNorthEastLatLng: LatLng
    val mNorthWestLatLng: LatLng

    constructor(mapGrid: MapGrid, southWestLatLng: LatLng, southEastLatLng: LatLng, northEastLatLng: LatLng, northWestLatLng: LatLng) {
        mGridMap = mapGrid
        mBounds = LatLngBounds(southWestLatLng, northEastLatLng)
        mSouthWestLatLng = southWestLatLng
        mSouthEastLatLng = southEastLatLng
        mNorthEastLatLng = northEastLatLng
        mNorthWestLatLng = northWestLatLng

        mPolygonOptions = PolygonOptions()
            .add(southWestLatLng, southEastLatLng, northEastLatLng, northWestLatLng)
            .strokeWidth(0f)
    }

    fun addMeasurement(measurement: Measurement) {
        sum += measurement.value
        number += 1
        calculateAverage()
        if (averagedValue > 0) {
            val newLevel = Measurement.getLevel(averagedValue, mGridMap.sensorThreshold)
            if (newLevel != level) {
                level = newLevel
                newColor = true
                mPolygonOptions = mPolygonOptions.fillColor(getColor())
            }

        }

    }

    private fun getColor(): Int {
        if (level == null) {
            return Color.TRANSPARENT
        } else {
            return getTransparentColor(MeasurementColor.colorForLevel(mGridMap.context, level!!))
        }
    }

    private fun getTransparentColor(color: Int) : Int{
        var alpha = Color.alpha(color)
        val red = Color.red(color);
        val green = Color.green(color);
        val blue = Color.blue(color);

        alpha += 100;

        return Color.argb(alpha, red, green, blue)
    }
    fun addPolygon(polygon: Polygon) {
        println("MARYSIA: adding polygon ${polygon}")
        if (mPolygon != null) {
            mPolygon?.remove()
        }
        mPolygon = polygon
        println("MARYSIA: polygon added? ${mPolygon}")
        newColor = false
    }

    fun remove() {
        println("MARYSIA: remove polygon ${mPolygon}")
        mPolygon?.remove()
    }
    fun inBounds(coordinates: LatLng) : Boolean {
        return mBounds.contains(coordinates)
    }

    private fun calculateAverage() {
        averagedValue = sum / number
    }


}

class MapGrid(val context: Context?, val map: GoogleMap, val sensorThreshold: SensorThreshold,  val mapWidth: Int, val mapHeight: Int) {
    private val DENSITY = 10

    var mGridSquares: HashMap<String, GridSquare?> = HashMap()
    var latNorth: Double
    var latSouth: Double
    var lonEast: Double
    var lonWest: Double
    var gridSizeX: Int
    var gridSizeY: Int

    init {
        val visibleRegion = map.projection.visibleRegion
        latNorth = visibleRegion.farLeft.latitude
        latSouth = visibleRegion.nearLeft.latitude
        lonEast = visibleRegion.farRight.longitude
        lonWest = visibleRegion.nearLeft.longitude

        val size = Math.min(mapWidth, mapHeight) / DENSITY

        gridSizeX = mapWidth / size
        gridSizeY = mapHeight / size

        val lonGridSide = (lonEast - lonWest) / gridSizeX
        val latGridSide = (latNorth - latSouth) / gridSizeY


        for (x in 1..gridSizeX) {
            for (y in 1..gridSizeY) {
                val southWestLatLng =
                    LatLng(latSouth + (y - 1) * latGridSide, lonWest + (x - 1) * lonGridSide)
                val southEastLatLng =
                    LatLng(latSouth + (y - 1) * latGridSide, lonWest + x * lonGridSide)
                val northEastLatLng = LatLng(latSouth + y * latGridSide, lonWest + x * lonGridSide)
                val northWestLatLng =
                    LatLng((latSouth + y * latGridSide), lonWest + (x - 1) * lonGridSide)
                println("MARYSIA: southWestLatLng: ${southWestLatLng}")
                mGridSquares["${x}_${y}"] = GridSquare(
                    this,
                    southWestLatLng,
                    southEastLatLng,
                    northEastLatLng,
                    northWestLatLng
                )
            }
        }
    }

        fun addMeasurement(measurement: Measurement) {
            if (measurement.latitude == null || measurement.longitude == null) return

            for (x in 1..gridSizeX) {
                for (y in 1..gridSizeY) {
                    val gridSquare = mGridSquares["${x}_${y}"]
                    gridSquare?.let {
                        if (gridSquare?.inBounds(
                                LatLng(
                                    measurement.latitude,
                                    measurement.longitude
                                )
                            )
                        ) {
                            gridSquare.addMeasurement(measurement)
                        }
                    }

                }
            }

            drawAverages()
        }

        private fun drawAverages() {
            for (x in 1..gridSizeX) {
                for (y in 1..gridSizeY) {

                    val gridSquare = mGridSquares["${x}_${y}"]
                    gridSquare?.let {
                        if (gridSquare.newColor) {
                            val polygon = map.addPolygon(gridSquare.mPolygonOptions)
                            println("MARYSIA: x_y ${x}_${y}")
                            gridSquare.addPolygon(polygon)
                        }
                    }

                }
            }
        }

        fun remove() {
            for (x in 1..gridSizeX) {
                for (y in 1..gridSizeY) {
                    println("MARYSIA: x_y ${x}_${y}")
                    mGridSquares["${x}_${y}"]?.remove()
                    mGridSquares["${x}_${y}"] = null
                }
            }
        }



//    }
}
