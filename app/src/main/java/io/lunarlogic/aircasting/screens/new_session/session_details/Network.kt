package io.lunarlogic.aircasting.screens.new_session.session_details

import android.net.wifi.WifiManager

class Network(val name: String, private val rssi: Int, frequencyInMzh: Int) {
    private val numberOfBars = 3

    companion object {
        val MAX_FREQUENCY = 2
    }

    val level: Int
    val frequency: Int

    init {
        level = calculateLevel()
        frequency = (frequencyInMzh * 0.001).toInt()
    }

    private fun calculateLevel(): Int {
        return WifiManager.calculateSignalLevel(rssi, numberOfBars)
    }
}
