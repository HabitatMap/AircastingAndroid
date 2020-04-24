package io.lunarlogic.aircasting.airbeam2

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import io.lunarlogic.aircasting.exceptions.AirBeam2ConnectionCloseFailed
import io.lunarlogic.aircasting.exceptions.AirBeam2ConnectionOpenFailed
import io.lunarlogic.aircasting.exceptions.ExceptionHandler
import io.lunarlogic.aircasting.lib.ResultCodes
import java.io.IOException
import java.util.*


class AirBeam2Connector(private val mExceptionHandler: ExceptionHandler) {
    var connectionStarted = false
    val SPP_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    fun connect(device: BluetoothDevice) {
        if (connectionStarted == false) {
            connectionStarted = true
            val thread = ConnectThread(device)
            thread.start()
        }
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(SPP_SERIAL)
        }

        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            bluetoothAdapter?.cancelDiscovery()

            try {
                mmSocket?.use { socket ->
                    socket.connect()

                    // The connection attempt succeeded. Perform work associated with
                    // the connection in a separate thread.
                    // manageMyConnectedSocket(socket)
                    println("Bluetooth CONNECTION SUCCEDED!")
                }
            } catch(e: IOException) {
                val message = mExceptionHandler.obtainMessage(ResultCodes.AIR_BEAM2_CONNECTION_OPEN_FAILED, AirBeam2ConnectionOpenFailed(e))
                message.sendToTarget()
                cancel()
            }
        }

        fun cancel() {
            connectionStarted = false
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                mExceptionHandler.handle(AirBeam2ConnectionCloseFailed(e))
            }
        }
    }
}