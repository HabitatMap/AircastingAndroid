package pl.llp.aircasting.data.api.util

object LogKeys {
    const val mobileActiveSessionsCount = "MobileActiveCount"
    const val followedSessionsCount = "FollowedCount"

    const val bluetoothReconnection = "BLUETOOTH_RECONNECTION"
}

val Any.TAG: String
    get() = this::class.java.simpleName