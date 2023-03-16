package pl.llp.aircasting.util

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import pl.llp.aircasting.data.model.Measurement

class SessionBoundingBox {
    companion object {
        fun get(measurements: List<Measurement>): LatLngBounds {
            var north = -90.0
            var south = 90.0
            var east = -180.0
            var west = 180.0

            measurements
                .filter { measurement -> measurement.latitude != null && measurement.longitude != null }
                .forEach { measurement ->
                    val latitude = measurement.latitude!!
                    val longitude = measurement.longitude!!

                    north = Math.max(north, latitude)
                    south = Math.min(south, latitude)
                    east = Math.max(east, longitude)
                    west = Math.min(west, longitude)
            }

            val southWest = LatLng(south, west)
            val northEast = LatLng(north, east)

            return LatLngBounds(southWest, northEast)
        }
    }
}
