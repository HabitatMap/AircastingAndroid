package io.lunarlogic.aircasting.screens.new_session.session_details

import android.net.wifi.WifiManager

class Network(val name: String, private val rssi: Int) {
    private val numberOfBars = 3

    val level: Int

    init {
        level = calculateLevel()
    }

    private fun calculateLevel(): Int {
        return WifiManager.calculateSignalLevel(rssi, numberOfBars)
    }
}
