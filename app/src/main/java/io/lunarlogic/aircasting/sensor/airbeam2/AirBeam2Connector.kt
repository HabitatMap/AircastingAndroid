package io.lunarlogic.aircasting.sensor.airbeam2

import android.bluetooth.BluetoothSocket
import io.lunarlogic.aircasting.exceptions.*
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.sensor.AirBeamConnector
import java.io.IOException
import java.io.OutputStream
import java.util.*


open class AirBeam2Connector(
    mSettings: Settings,
    private val mErrorHandler: ErrorHandler
): AirBeamConnector() {
    private val mAirBeamConfigurator = AirBeam2Configurator(mSettings)
    private val mAirBeam2Reader = AirBeam2Reader(mErrorHandler)

    private val SPP_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var mThread: ConnectThread? = null
    private val ESTIMATED_CONNECTING_TIME_SECONDS = 3000L

    override fun start(deviceItem: DeviceItem) {
        mThread = ConnectThread(deviceItem)
        mThread?.start()
    }

    override fun stop() {
        mThread?.cancel()
    }

    override fun sendAuth(sessionUUID: String) {
        mThread?.sendAuth(sessionUUID)
    }

    override fun configureSession(session: Session, wifiSSID: String?, wifiPassword: String?) {
        mThread?.configureSession(session, wifiSSID, wifiPassword)
    }

    override fun reconnectMobileSession() {
        mThread?.reconnectMobileSession()
    }

    override fun triggerSDCardDownload() {
        // AirBeam2 can't do sync
    }

    override fun clearSDCard() {
        // AirBeam2 don't have SD card
    }

    private inner class ConnectThread(private val deviceItem: DeviceItem) : Thread() {
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            val device = deviceItem.bluetoothDevice
            device?.createRfcommSocketToServiceRecord(SPP_SERIAL)
        }

        private lateinit var mOutputStream: OutputStream

        override fun run() {
            connectionEstablished.set(false)

            try {
                mmSocket?.use { socket ->
                    socket.connect()
                    // wait until connection is finished before sending anything to AirBeam
                    sleep(ESTIMATED_CONNECTING_TIME_SECONDS)

                    mOutputStream = socket.outputStream

                    onConnectionSuccessful(deviceItem)
                    connectionEstablished.set(true)
                    mAirBeam2Reader.run(socket.inputStream)
                }
            } catch(e: IOException) {
                val deviceId = deviceItem.id
                onDisconnected(deviceId)
                onConnectionFailed(deviceId)

                if (!cancelStarted.get() && !connectionEstablished.get()) {
                    mErrorHandler.handle(AirBeamConnectionOpenFailed(e))
                    cancel()
                }
            } catch(e: Exception) {
                onConnectionFailed(deviceItem.id)

                mErrorHandler.handle(UnknownError(e))
                cancel()
            }
        }

        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                mErrorHandler.handle(AirBeam2ConnectionCloseFailed(e))
            }
        }

        fun configureSession(session: Session, wifiSSID: String?, wifiPassword: String?) {
            try {
                mAirBeamConfigurator.configure(session, wifiSSID, wifiPassword, mOutputStream)
            } catch (e: IOException) {
                mErrorHandler.handle(AirBeam2ConfiguringFailed(e))
            }
        }

        fun sendAuth(sessionUUID: String) {
            mAirBeamConfigurator.sendAuth(sessionUUID, mOutputStream)
        }

        fun reconnectMobileSession() {
            mAirBeamConfigurator.reconnectMobileSession(mOutputStream)
        }
    }
}
