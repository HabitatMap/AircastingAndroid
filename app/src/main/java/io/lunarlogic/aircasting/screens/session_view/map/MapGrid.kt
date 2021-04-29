package io.lunarlogic.aircasting.screens.session_view.map

import android.content.Context
import android.graphics.Color
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.LatLngBounds
import com.google.android.libraries.maps.model.Polygon
import com.google.android.libraries.maps.model.PolygonOptions
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.SensorThreshold

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

class MapGrid(val context: Context?, val mMap: GoogleMap, val sensorThreshold: SensorThreshold, mapWidth: Int, mapHeight: Int) {
    private val DENSITY = 10

    var mGridSquares: HashMap<String, GridSquare?> = HashMap()
    var mVisibleRegion: VisibleRegionCoordinates

    var gridSizeX: Int
    var gridSizeY: Int

    init {
        mVisibleRegion = getVisibleRegionCoordinates()

        val size = Math.min(mapWidth, mapHeight) / DENSITY

        gridSizeX = mapWidth / size
        gridSizeY = mapHeight / size

        val lonGridSide = (mVisibleRegion.lonEast - mVisibleRegion.lonWest) / gridSizeX
        val latGridSide = (mVisibleRegion.latNorth - mVisibleRegion.latSouth) / gridSizeY


        for (x in 1..gridSizeX) {
            for (y in 1..gridSizeY) {
                val southWestLatLng =
                    LatLng(southLatitude(latGridSide, y), westLongitude(lonGridSide, x))
                val southEastLatLng =
                    LatLng(southLatitude(latGridSide, y), eastLongitude(lonGridSide, x))
                val northEastLatLng =
                    LatLng(northLatitude(latGridSide, y), eastLongitude(lonGridSide, x))
                val northWestLatLng =
                    LatLng(northLatitude(latGridSide, y), westLongitude(lonGridSide, x))

                setGridSquare(x, y, GridSquare(
                    southWestLatLng,
                    southEastLatLng,
                    northEastLatLng,
                    northWestLatLng
                ))
            }
        }
    }

    fun drawHeatMap(measurements: List<Measurement>) {
        measurements.forEach { measurement ->
            assignMeasurementToSquare(measurement)
        }
        drawAverages()
    }

    fun addMeasurement(measurement: Measurement) {
        assignMeasurementToSquare(measurement)
        drawAverages()
    }

    private fun eastLongitude(longitudeSize: Double, x: Int) : Double {
        return mVisibleRegion.lonWest + x * longitudeSize
    }

    private fun westLongitude(longitudeSize: Double, x: Int) : Double {
        return mVisibleRegion.lonWest + (x - 1) * longitudeSize
    }

    private fun northLatitude(latitudeSize: Double, y: Int) : Double {
        return mVisibleRegion.latSouth + y * latitudeSize
    }

    private fun southLatitude(latitudeSize: Double, y: Int) : Double {
        return mVisibleRegion.latSouth + (y - 1) * latitudeSize
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
//        }

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

        if(measurement.latitude >= middleLat) {
            newIndexYstart = middleY + 1
        } else {
            newIndexYend = middleY
        }

        return getSquareXY(measurement, newIndexXstart, newIndexXend, newIndexYstart, newIndexYend)
    }

    private fun assignMeasurementToSquare(measurement: Measurement) {
        val squareXY = getSquareXY(measurement, 0, gridSizeX - 1, 0, gridSizeY - 1)
        squareXY?.let {
            val gridSquare = getGridSquare(squareXY.first, squareXY.second)
            gridSquare?.addMeasurement(measurement)
        }
    }

    private fun drawAverages() {
        for (x in 1..gridSizeX) {
            for (y in 1..gridSizeY) {
                val gridSquare = getGridSquare(x, y)
                gridSquare?.drawPolygon()
            }
        }
    }

    fun remove() {
        for (x in 1..gridSizeX) {
            for (y in 1..gridSizeY) {
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
}
