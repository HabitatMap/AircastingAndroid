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
 * Created by Maria Turnau on 23/04/2021.
 */

class MapGrid(val context: Context?, val map: GoogleMap, val sensorThreshold: SensorThreshold, val mapWidth: Int, val mapHeight: Int) {
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
                    southWestLatLng,
                    southEastLatLng,
                    northEastLatLng,
                    northWestLatLng
                )
            }
        }
    }

    fun drawMap(measurements: List<Measurement>) {
        measurements.forEach { measurement ->
            assignMeasurementToSquare(measurement)
        }
        drawAverages()
        // itearate through the grid, assign measurements (addMeasurement on grid square) and drawAverages
    }

    fun addMeasurement(measurement: Measurement) {
        assignMeasurementToSquare(measurement)
        drawAverages()
    }

    private fun assignMeasurementToSquare(measurement: Measurement) {
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
    }
    private fun drawAverages() {
        for (x in 1..gridSizeX) {
            for (y in 1..gridSizeY) {

                val gridSquare = mGridSquares["${x}_${y}"]
                gridSquare?.let {
                    gridSquare.drawPolygon()
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


    inner class GridSquare {
//        private var mGridMap: MapGrid
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

        constructor(southWestLatLng: LatLng, southEastLatLng: LatLng, northEastLatLng: LatLng, northWestLatLng: LatLng) {
//            mGridMap = mapGrid
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
                val newLevel = Measurement.getLevel(averagedValue, sensorThreshold)
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
                return getTransparentColor(MeasurementColor.colorForLevel(context, level!!))
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

        fun drawPolygon() {
            if (shouldDrawPolygon()) {
                addPolygon()
            }
        }

        private fun shouldDrawPolygon() : Boolean {
            return newColor || (mPolygon == null  && averagedValue > 0)
        }

        fun addPolygon() {
            val polygon = map.addPolygon(mPolygonOptions)
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

}
