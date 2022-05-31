package pl.llp.aircasting.data.model.sessions

import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import java.util.*

open class Session(
    open val type: Type = Type.FIXED,
    open var followedAt: Date? = null,
    open var locationless: Boolean = false,
    open var status: Status = Status.RECORDING
) {


    val followed get() = followedAt != null
    val tab
        get() = run {
            if (followed) SessionsTab.FOLLOWING else {
                if (isFixed()) SessionsTab.FIXED else {
                    when (status) {
                        Status.FINISHED -> SessionsTab.MOBILE_DORMANT
                        Status.RECORDING -> SessionsTab.MOBILE_ACTIVE
                        else -> SessionsTab.MOBILE_DORMANT
                    }
                }
            }
        }

    fun isFixed(): Boolean {
        return type == Type.FIXED
    }
    enum class Type(val value: Int){
        MOBILE(0),
        FIXED(1);

        companion object {
            fun fromInt(value: Int) = values().first { it.value == value }
        }
    }

    enum class Status(val value: Int){
        NEW(-1),
        RECORDING(0),
        FINISHED(1),
        DISCONNECTED(2);

        companion object {
            fun fromInt(value: Int) = values().first { it.value == value }
        }
    }

    enum class StreamingMethod(val value: Int) {
        CELLULAR(0),
        WIFI(1)
    }

    class Location(val latitude: Double, val longitude: Double) {
        companion object {
            // for indoor fixed sessions
            val FAKE_LOCATION = Location(200.0, 200.0)

            // if for some reason current location is not available
            val DEFAULT_LOCATION = Location(40.7128, -74.0060)

            fun get(location: android.location.Location?, locationless: Boolean = false): Location {
                if (locationless) {
                    return FAKE_LOCATION
                }

                if (location == null) {
                    return DEFAULT_LOCATION
                }

                return Location(location.latitude, location.longitude)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (other == null || other !is Location) return false

            return latitude == other.latitude && longitude == other.longitude
        }

        override fun hashCode(): Int {
            var result = latitude.hashCode()
            result = 31 * result + longitude.hashCode()
            return result
        }
    }
}
