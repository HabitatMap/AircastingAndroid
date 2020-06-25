package io.lunarlogic.aircasting.sensor.airbeam2

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import io.lunarlogic.aircasting.events.ApplicationClosed
import io.lunarlogic.aircasting.events.ConfigureSession
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.exceptions.*
import io.lunarlogic.aircasting.lib.ResultCodes
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.ConnectingAirBeamController
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem
import io.lunarlogic.aircasting.sensor.Session
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


open class AirBeam2Connector(
    private val mErrorHandler: ErrorHandler,
    private val mAirBeamConfigurator: AirBeam2Configurator,
    private val mAirBeam2Reader: AirBeam2Reader
) {
    private val connectionStarted = AtomicBoolean(false)
    private val cancelStarted = AtomicBoolean(false)
    private val SPP_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var mThread: ConnectThread? = null
    private val ESTIMATED_CONNECTING_TIME_SECONDS = 3000L

    lateinit var listener: ConnectingAirBeamController.Listener

    open fun connect(deviceItem: DeviceItem, sessionType: Session.Type) {
        if (connectionStarted.get() == false) {
            connectionStarted.set(true)
            EventBus.getDefault().register(this);
            mThread = ConnectThread(deviceItem, sessionType)
            mThread?.start()
        }
    }

    fun cancel() {
        mThread?.cancel()
    }

    fun configureSession(session: Session) {
        mThread?.configureSession(session)
    }

    private inner class ConnectThread(private val deviceItem: DeviceItem, private val sessionType: Session.Type) : Thread() {
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            val device = deviceItem.bluetoothDevice
            device.createRfcommSocketToServiceRecord(SPP_SERIAL)
        }

        private lateinit var mOutputStream: OutputStream

        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            bluetoothAdapter?.cancelDiscovery()

            try {
                mmSocket?.use { socket ->
                    socket.connect()
                    // wait until connection is finished before sending anything to AirBeam
                    sleep(ESTIMATED_CONNECTING_TIME_SECONDS)

                    mOutputStream = socket.outputStream

                    listener.onConnectionSuccessful(deviceItem.id)
                    mAirBeam2Reader.run(socket.inputStream)
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
                mErrorHandler.handle(e)
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

        fun configureSession(session: Session) {
            try {
                mAirBeamConfigurator.configureSessionType(session, mOutputStream)
                if (session.isFixed()) {
                    mAirBeamConfigurator.configureFixedSessionDetails(
                        session.location!!,
                        session.streamingMethod!!,
                        "slimaki-guest",
                        "testtest",
                        mOutputStream
                    )
                }
            } catch (e: IOException) {
                mErrorHandler.handle(AirBeam2ConfiguringFailed(e))
            }
        }
    }

    @Subscribe
    fun onMessageEvent(event: ConfigureSession) {
        configureSession(event.session)
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