package io.lunarlogic.aircasting.screens.new_session.session_details

import android.net.wifi.WifiManager

class Network(val name: String, val rssi: Int) {
    val NUMBER_OF_BARS = 5

    fun calculateLevel(): Int {
        return WifiManager.calculateSignalLevel(rssi, NUMBER_OF_BARS)
    }
}
