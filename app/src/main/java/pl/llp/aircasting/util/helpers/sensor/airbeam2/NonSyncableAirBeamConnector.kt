package pl.llp.aircasting.util.helpers.sensor.airbeam2

import android.bluetooth.BluetoothSocket
import android.util.Log
import pl.llp.aircasting.data.api.util.LogKeys.bluetoothReconnection
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.exceptions.*
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamConnector
import java.io.IOException
import java.io.OutputStream
import java.util.*
import javax.inject.Inject

open class NonSyncableAirBeamConnector @Inject constructor(
    private val mErrorHandler: ErrorHandler,
    bluetoothManager: BluetoothManager,
    private val mAirBeamConfigurator: AirBeam2Configurator,
    private val mAirBeam2Reader: AirBeam2Reader,
) : AirBeamConnector(bluetoothManager) {

    private val SPP_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var mThread: ConnectThread? = null
    private val ESTIMATED_CONNECTING_TIME_SECONDS = 5000L

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
            } catch (e: IOException) {
                Log.e(bluetoothReconnection, e.stackTraceToString())
                 mErrorHandler.handle(SensorDisconnectedError("called from Airbeam2Connector: IOException connection established ${connectionEstablished.get()}"))
                onDisconnected(deviceItem)

                if (!cancelStarted.get() && !connectionEstablished.get()) {
                    onConnectionFailed(deviceItem)
                    mErrorHandler.handle(AirBeamConnectionOpenFailed(e))
                    cancel()
                }
            } catch (e: Exception) {
                onConnectionFailed(deviceItem)

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
