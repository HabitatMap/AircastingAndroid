package io.lunarlogic.aircasting.bluetooth

import android.bluetooth.BluetoothAdapter
import io.lunarlogic.aircasting.exceptions.BluetoothNotSupportedException

class BluetoothManager {
    val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    @Throws(BluetoothNotSupportedException::class)
    fun isBluetoothEnabled() : Boolean {
        if (adapter == null) {
            throw BluetoothNotSupportedException()
        }

        return adapter?.isEnabled == true
    }

    fun startDiscovery() {
        adapter?.startDiscovery()
    }
}