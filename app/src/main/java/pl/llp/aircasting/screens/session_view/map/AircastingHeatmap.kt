package pl.llp.aircasting.screens.session_view.map

import android.content.Context
import android.graphics.Color
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.*
import pl.llp.aircasting.lib.MeasurementColor
import pl.llp.aircasting.models.Measurement
import pl.llp.aircasting.models.SensorThreshold

/**
 * This is a custom "heatmap" for the Aircasting maps. The way it works is similar to what we already have
 * on the website when you switch on "Crowdmap" but it works for single session and for mobile active measurements.
 * We redraw the heatmap with every zoom and pan, because the grid squares are always calculated on the visible part of the map.
 * We divide the visible are of the map into virtual squares (mGridSquares). We calculate the size of the single square
 * using heatmap density and size of the visible area on the screen.
 * For example, a "portrait" map may be divided into 10x12 grid. In this case gridSizeX = 10 and gridSizeY = 12.
 * For each grid square we calculate coordinates for each corner so we can use them to draw a Polygon if needed.
 *
 * NW-----NE
 * |      |
 * |      |
 * SW-----SE
 *
 * On heatmap redraw, we go through all measurements we want to map and assign them to squares. The algorithm for this
 * is a simple binary search based on grid squares coordinates. On assigning measurements, we caclulate the average
 * value of each square and assign an appropriate level.
 *
 * With every new measurement, the average of appropriate square is recalculated and polygon redrawn
 * (only if the level has changed)
 */

class AircastingHeatmap(val context: Context?, val mMap: GoogleMap, val sensorThreshold: SensorThreshold, mapWidth: Int, mapHeight: Int) {
    private val DENSITY = 10

    var mGridSquares: HashMap<String, GridSquare?> = HashMap()
    var mVisibleRegion: VisibleRegionCoordinates

    var mGridSizeX: Int
    var mGridSizeY: Int

    init {
        mVisibleRegion = getVisibleRegionCoordinates()

        val size = Math.min(mapWidth, mapHeight) / DENSITY

        mGridSizeX = mapWidth / size
        mGridSizeY = mapHeight / size

       initGrid()
    }

    fun drawHeatMap(measurements: List<Measurement>) {
        measurements.forEach { measurement ->
            assignMeasurementToSquare(measurement)
        }
        drawPolygons()
    }

    fun addMeasurement(measurement: Measurement) {
        assignMeasurementToSquare(measurement)
        drawPolygons()
    }

    private fun initGrid() {

        val lonGridSize = (mVisibleRegion.lonEast - mVisibleRegion.lonWest) / mGridSizeX
        val latGridSize = (mVisibleRegion.latNorth - mVisibleRegion.latSouth) / mGridSizeY

        for (x in 1..mGridSizeX) {
            for (y in 1..mGridSizeY) {
                val gridCalculator = GridCalculator(x, y, mVisibleRegion, lonGridSize, latGridSize)

                setGridSquare(x, y, GridSquare(
                    gridCalculator.southWestLatLng(),
                    gridCalculator.southEastLatLng(),
                    gridCalculator.northEastLatLng(),
                    gridCalculator.northWestLatLng()
                ))
            }
        }
    }

    private fun getVisibleRegionCoordinates(): VisibleRegionCoordinates {
        val visibleRegion = mMap.projection.visibleRegion

        return VisibleRegionCoordinates(
            visibleRegion.farLeft.latitude,
            visibleRegion.nearLeft.latitude,
            visibleRegion.farRight.longitude,
            visibleRegion.nearLeft.longitude
        )
    }

    private fun getGridSquare(x: Int, y: Int): GridSquare? {
        return mGridSquares[gridSquareKey(x, y)]
    }

    private fun setGridSquare(x: Int, y: Int, gridSquare: GridSquare?) {
        mGridSquares[gridSquareKey(x, y)] = gridSquare
    }

    private fun gridSquareKey(x: Int, y: Int): String {
        return "${x}_${y}"
    }
    private fun getSquareXY(measurement: Measurement, indexXstart: Int, indexXend: Int, indexYstart: Int, indexYend: Int): Pair<Int, Int>? {
        val middleX = indexXstart  + (indexXend - indexXstart) / 2
        val middleY = indexYstart  + (indexYend - indexYstart) / 2
        val middleSquare = getGridSquare(middleX + 1, middleY + 1)

        if (middleSquare == null || measurement.longitude == null || measurement.latitude == null) return null

        // We check every time if point is in binds of middle square. It may be the last square checked (indexStart == indexEnd)
            if (middleSquare.inBounds(LatLng(measurement.latitude, measurement.longitude))) {
                return Pair(middleX + 1, middleY + 1)
            }
        // if this is the last square checked and point is not in its bounds, return null
            if (indexXstart == indexXend && indexYstart == indexYend) {
                return null
            }

        var newIndexXstart = indexXstart
        var newIndexYstart = indexYstart
        var newIndexXend = indexXend
        var newIndexYend = indexYend

        val middleLon = middleSquare?.mNorthEastLatLng?.longitude
        val middleLat = middleSquare?.mNorthEastLatLng?.latitude

        if (measurement.longitude == null || measurement.latitude == null || middleLon == null || middleLat == null) return null

        if (measurement.longitude >= middleLon) {
            newIndexXstart = middleX + 1
        } else {
            newIndexXend = middleX
        }

        if (measurement.latitude >= middleLat) {
            newIndexYstart = middleY + 1
        } else {
            newIndexYend = middleY
        }

        return getSquareXY(measurement, newIndexXstart, newIndexXend, newIndexYstart, newIndexYend)
    }

    private fun assignMeasurementToSquare(measurement: Measurement) {
        val squareXY = getSquareXY(measurement, 0, mGridSizeX - 1, 0, mGridSizeY - 1)
        squareXY?.let {
            val gridSquare = getGridSquare(squareXY.first, squareXY.second)
            gridSquare?.addMeasurement(measurement)
        }
    }

    private fun drawPolygons() {
        for (x in 1..mGridSizeX) {
            for (y in 1..mGridSizeY) {
                val gridSquare = getGridSquare(x, y)
                gridSquare?.drawPolygon()
            }
        }
    }

    fun remove() {
        for (x in 1..mGridSizeX) {
            for (y in 1..mGridSizeY) {
                removeGridSquare(x, y)
            }
        }
    }

    private fun removeGridSquare(x: Int, y: Int) {
        getGridSquare(x, y)?.remove()
        setGridSquare(x, y, null)
    }

    inner class GridSquare {
        private var mSum: Double = 0.0
        private var mNumber: Int = 0
        private var mAveragedValue: Double? = null
        private var mLevel: Measurement.Level? = null
        private var mPolygonOptions: PolygonOptions
        private var mPolygon: Polygon? = null
        private var mNewColor: Boolean = false

        private val mSouthWestLatLng: LatLng
        private val mSouthEastLatLng: LatLng
        val mNorthEastLatLng: LatLng
        private val mNorthWestLatLng: LatLng

        constructor(southWestLatLng: LatLng, southEastLatLng: LatLng, northEastLatLng: LatLng, northWestLatLng: LatLng) {
            mSouthWestLatLng = southWestLatLng
            mSouthEastLatLng = southEastLatLng
            mNorthEastLatLng = northEastLatLng
            mNorthWestLatLng = northWestLatLng

            mPolygonOptions = PolygonOptions()
                .add(southWestLatLng, southEastLatLng, northEastLatLng, northWestLatLng)
                .strokeWidth(0f)
                .strokeColor(Color.TRANSPARENT)
        }

        fun addMeasurement(measurement: Measurement) {
            mSum += measurement.value
            mNumber += 1
            calculateAverage()
            mAveragedValue?.let { averagedValue ->
                val newLevel = Measurement.getLevel(averagedValue, sensorThreshold)
                if (newLevel != mLevel) {
                    mLevel = newLevel
                    mNewColor = true
                    mPolygonOptions = mPolygonOptions.fillColor(getColor())
                }
            }
        }

        fun drawPolygon() {
            if (shouldDrawPolygon()) {
                addPolygon()
            }
        }

        fun remove() {
            mPolygon?.remove()
        }

        fun inBounds(coordinates: LatLng) : Boolean {
            val bounds = LatLngBounds(mSouthWestLatLng, mNorthEastLatLng)
            return bounds.contains(coordinates)
        }

        private fun addPolygon() {
            val polygon = mMap.addPolygon(mPolygonOptions)
            if (mPolygon != null) {
                mPolygon?.remove()
            }
            mPolygon = polygon
            mNewColor = false
        }

        private fun getColor(): Int {
            return if (mLevel == null) {
                Color.TRANSPARENT
            } else {
                getTransparentColor(MeasurementColor.colorForLevel(context, mLevel!!))
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

        private fun shouldDrawPolygon() : Boolean {
            return mNewColor || (mPolygon == null && mAveragedValue != null)
        }

        private fun calculateAverage() {
            mAveragedValue = mSum / mNumber
        }
    }

    class VisibleRegionCoordinates(val latNorth: Double, val latSouth: Double, var lonEast: Double, val lonWest: Double)

    class GridCalculator(val x: Int, val y: Int, val visibleRegion: VisibleRegionCoordinates, val lonGridSize: Double, val latGridSize: Double) {
        fun southWestLatLng(): LatLng {
            return LatLng(southLatitude(), westLongitude())
        }
        fun southEastLatLng(): LatLng {
            return LatLng(southLatitude(), eastLongitude())
        }

        fun northEastLatLng(): LatLng {
           return LatLng(northLatitude(), eastLongitude())
        }

        fun northWestLatLng(): LatLng {
            return LatLng(northLatitude(), westLongitude())
        }

        private fun eastLongitude() : Double {
            return visibleRegion.lonWest + x * lonGridSize
        }

        private fun westLongitude() : Double {
            return visibleRegion.lonWest + (x - 1) * lonGridSize
        }

        private fun northLatitude() : Double {
            return visibleRegion.latSouth + y * latGridSize
        }

        private fun southLatitude() : Double {
            return visibleRegion.latSouth + (y - 1) * latGridSize
        }
    }
}
