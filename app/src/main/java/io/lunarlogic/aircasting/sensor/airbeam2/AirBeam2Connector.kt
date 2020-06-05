package io.lunarlogic.aircasting.sensor.airbeam2

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import io.lunarlogic.aircasting.events.ApplicationClosed
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.exceptions.*
import io.lunarlogic.aircasting.lib.ResultCodes
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.ConnectingAirBeamController
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class AirBeam2Connector(
    private val mErrorHandler: ErrorHandler,
    private val mListener: ConnectingAirBeamController.Listener
) {
    private val connectionStarted = AtomicBoolean(false)
    private val cancelStarted = AtomicBoolean(false)
    private val SPP_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val mAirBeamConfigurator = AirBeam2Configurator()
    private val mAirBeam2Reader = AirBeam2Reader()
    private var mThread: ConnectThread? = null
    private val ESTIMATED_CONNECTING_TIME_SECONDS = 3000L

    fun connect(deviceItem: DeviceItem) {
        if (connectionStarted.get() == false) {
            connectionStarted.set(true)
            EventBus.getDefault().register(this);
            mThread = ConnectThread(deviceItem)
            mThread?.start()
        }
    }

    fun cancel() {
        mThread?.cancel()
    }

    private inner class ConnectThread(private val deviceItem: DeviceItem) : Thread() {
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            val device = deviceItem.bluetoothDevice
            device.createRfcommSocketToServiceRecord(SPP_SERIAL)
        }

        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            bluetoothAdapter?.cancelDiscovery()

            try {
                mmSocket?.use { socket ->
                    socket.connect()
                    // wait until connection is finished before sending anything to AirBeam
                    sleep(ESTIMATED_CONNECTING_TIME_SECONDS)

                    val outputStream = socket.outputStream
                    try {
                        mAirBeamConfigurator.configureBluetooth(outputStream)
                    } catch (e: IOException) {
                        mErrorHandler.handle(AirBeam2ConfiguringFailed(e))
                    }

                    mListener.onConnectionSuccessful(deviceItem.id)
                    mAirBeam2Reader.run(socket)
                }
            } catch(e: IOException) {
                if (cancelStarted.get() == false) {
                    val message = mErrorHandler.obtainMessage(
                        ResultCodes.AIR_BEAM2_CONNECTION_OPEN_FAILED,
                        AirBeam2ConnectionOpenFailed(e)
                    )
                    message.sendToTarget()
                    cancel()
                }
            } catch(e: SensorResponseParsingError) {
                val message = mErrorHandler.obtainMessage(ResultCodes.SENSOR_RESPONSE_PARSING_ERROR, e)
                message.sendToTarget()
            } catch(e: Exception) {
                val message = mErrorHandler.obtainMessage(ResultCodes.AIRCASTING_UNKNOWN_ERROR, UnknownError(e))
                message.sendToTarget()
                cancel()
            }
        }

        fun cancel() {
            if (cancelStarted.get() == false) {
                cancelStarted.set(true)
                connectionStarted.set(false)
                EventBus.getDefault().unregister(this);
                try {
                    mmSocket?.close()
                } catch (e: IOException) {
                    mErrorHandler.handle(AirBeam2ConnectionCloseFailed(e))
                }
            }
        }
    }

    @Subscribe
    fun onMessageEvent(event: ApplicationClosed) {
        cancel()
    }

    @Subscribe
    fun onMessageEvent(event: StopRecordingEvent) {
        cancel()
    }
}