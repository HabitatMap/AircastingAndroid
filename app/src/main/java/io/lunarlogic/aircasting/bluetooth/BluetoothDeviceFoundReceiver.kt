package io.lunarlogic.aircasting.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.lunarlogic.aircasting.airbeam2.AirBeam2Connector
import io.lunarlogic.aircasting.exceptions.ExceptionHandler

class BluetoothDeviceFoundReceiver(mExceptionHandler: ExceptionHandler): BroadcastReceiver() {
    val airBeam2Connector = AirBeam2Connector(mExceptionHandler)

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device: BluetoothDevice =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                // TODO: handle more devices - screen for choosing
                println("Bluetooth new device found name: " + device.name + " address: " + device.address)
                if (device.name == "Airbeam2:0018961070D6") {
                    airBeam2Connector.connect(device)
                }
            }
        }
    }
}