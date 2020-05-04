package io.lunarlogic.aircasting.sensor.airbeam2

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Messenger
import io.lunarlogic.aircasting.bluetooth.BluetoothService
import io.lunarlogic.aircasting.exceptions.AirBeam2ConnectionCloseFailed
import io.lunarlogic.aircasting.exceptions.AirBeam2ConnectionOpenFailed
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.UnknownError
import io.lunarlogic.aircasting.lib.ResultCodes
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.ConnectingAirBeamController
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class AirBeam2Connector(
    private val mErrorHandler: ErrorHandler,
    private val mListener: ConnectingAirBeamController.Listener
) {
    val connectionStarted = AtomicBoolean(false)
    val SPP_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    val bluetoothService = BluetoothService()

    fun connect(device: BluetoothDevice) {
        if (connectionStarted.get() == false) {
            connectionStarted.set(true)
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

                    mListener.onConnectionSuccessful()
                    bluetoothService.run(socket)
                }
            } catch(e: IOException) {
                val message = mErrorHandler.obtainMessage(ResultCodes.AIR_BEAM2_CONNECTION_OPEN_FAILED, AirBeam2ConnectionOpenFailed(e))
                message.sendToTarget()
                cancel()
            } catch(e: Exception) {
                val message = mErrorHandler.obtainMessage(ResultCodes.AIRCASTING_UNKNOWN_ERROR, UnknownError(e))
                message.sendToTarget()
                cancel()
            }
        }

        fun cancel() {
            connectionStarted.set(false)
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                mErrorHandler.handle(AirBeam2ConnectionCloseFailed(e))
            }
        }
    }
}