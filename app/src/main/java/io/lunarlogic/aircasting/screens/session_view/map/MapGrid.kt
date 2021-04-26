package io.lunarlogic.aircasting.screens.session_view.map

import android.content.Context
import android.graphics.Color
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.Polygon
import com.google.android.libraries.maps.model.PolygonOptions
import io.lunarlogic.aircasting.exceptions.MapGridSquareSearchException
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.SensorThreshold

/**
 * Created by Maria Turnau on 23/04/2021.
 */

class MapGrid(val context: Context?, val map: GoogleMap, val sensorThreshold: SensorThreshold, mapWidth: Int, mapHeight: Int) {
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

    private fun getGridSquare(x: Int, y: Int): GridSquare? {
        return mGridSquares[gridSquareKey(x, y)]
    }

    private fun setGridSquare(x: Int, y: Int, gridSquare: GridSquare?) {
        mGridSquares[gridSquareKey(x, y)] = gridSquare
    }

    private fun gridSquareKey(x: Int, y: Int): String {
        return "${x}_${y}"
    }
    private fun getSquareXY(measurement: Measurement, indexXstart: Int, indexXend: Int, indexYstart: Int, indexYend: Int): Pair<Int, Int> {
        if (indexXstart == indexXend && indexYstart == indexYend) return Pair(indexXstart + 1, indexYstart + 1)

        var newIndexXstart = indexXstart
        var newIndexYstart = indexYstart
        var newIndexXend = indexXend
        var newIndexYend = indexYend

        val middleX = indexXstart  + (indexXend - indexXstart) / 2
        val middleY = indexYstart  + (indexYend - indexYstart) / 2
        val middleSquare = mGridSquares["${middleX+1}_${middleY+1}"]
        val middleLon = middleSquare?.mNorthEastLatLng?.longitude
        val middleLat = middleSquare?.mNorthEastLatLng?.latitude

        if (measurement.longitude == null || measurement.latitude == null || middleLon == null || middleLat == null) throw MapGridSquareSearchException()

        if(measurement.longitude >= middleLon) {
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
        val gridSquare = getGridSquare(squareXY.first, squareXY.second)

        gridSquare?.addMeasurement(measurement)
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
        private var mAveragedValue: Double = 0.0
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
            if (mAveragedValue > 0) {
                val newLevel = Measurement.getLevel(mAveragedValue, sensorThreshold)
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

        private fun addPolygon() {
            val polygon = map.addPolygon(mPolygonOptions)
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
            return mNewColor || (mPolygon == null  && mAveragedValue > 0)
        }

        private fun calculateAverage() {
            mAveragedValue = mSum / mNumber
        }
    }
}
