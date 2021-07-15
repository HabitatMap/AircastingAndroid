package pl.llp.aircasting.sensor

import android.bluetooth.BluetoothAdapter
import pl.llp.aircasting.events.*
import pl.llp.aircasting.lib.safeRegister
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.exceptions.SensorDisconnectedError
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.timerTask

abstract class AirBeamConnector {
    interface Listener {
        fun onConnectionSuccessful(deviceItem: DeviceItem, sessionUUID: String?)
        fun onConnectionFailed(deviceItem: DeviceItem)
        fun onDisconnect(deviceId: String)
    }

    private var mListener: Listener? = null
    private val CONNECTION_TIMEOUT = 10000L

    protected val connectionStarted = AtomicBoolean(false)
    protected val cancelStarted = AtomicBoolean(false)
    protected val connectionEstablished = AtomicBoolean(false)
    protected val connectionTimedOut = AtomicBoolean(false)

    protected var mDeviceItem: DeviceItem? = null
    protected var mSessionUUID: String? = null

    abstract protected fun start(deviceItem: DeviceItem)
    abstract protected fun stop()
    abstract protected fun sendAuth(sessionUUID: String)
    abstract protected fun configureSession(session: Session, wifiSSID: String?, wifiPassword: String?)

    fun connect(deviceItem: DeviceItem, sessionUUID: String? = null) {
        println("MARYSIA: trying to connect, AirbeamConnector connect()")

        mDeviceItem = deviceItem
        mSessionUUID = sessionUUID

        connectionTimedOut.set(false)

        // Cancel discovery because it otherwise slows down the connection.
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter?.cancelDiscovery()

        if (!connectionStarted.get()) {
            failAfterTimeout(deviceItem)
            connectionStarted.set(true)
            registerToEventBus()
            start(deviceItem)
        }
    }

    abstract fun reconnectMobileSession()
    abstract fun triggerSDCardDownload()
    abstract fun clearSDCard()

    private var mTimerTask: TimerTask? = null

    private fun failAfterTimeout(deviceItem: DeviceItem) {
        mTimerTask = timerTask {
            if (connectionEstablished.get() == false) {
                connectionTimedOut.set(true)
                mListener?.onConnectionFailed(deviceItem)
            }
        }
        Timer().schedule(mTimerTask, CONNECTION_TIMEOUT)
    }

    fun disconnect() {
        unregisterFromEventBus()

        if (!cancelStarted.get()) {
            cancelStarted.set(true)
            connectionStarted.set(false)
            stop()
            cancelStarted.set(false)
        }
    }

    fun registerListener(listener: Listener) {
        mListener = listener
    }

    fun onConnectionSuccessful(deviceItem: DeviceItem) {
        println("MARYSIA: Airbeam2Conector onConnectionSuccessful")
        mDeviceItem = deviceItem
        println("MARYSIA conected device item: ${deviceItem}")
        println("MARYSIA conected device item: ${deviceItem.id}")
        println("MARYSIA conected device item: ${deviceItem.address}")
        println("MARYSIA conected device item: ${deviceItem.bluetoothDevice}")
        println("MARYSIA conected device item: ${deviceItem.address}")
        connectionEstablished.set(true)
        mListener?.onConnectionSuccessful(deviceItem, mSessionUUID)
    }

    fun onConnectionFailed(deviceItem: DeviceItem) {
        println("MARYSIA: Airbeam2Conector onConnectionFailed")
        mTimerTask?.cancel()
        if (connectionTimedOut.get() == false) {
            mListener?.onConnectionFailed(deviceItem)
        }
    }

    fun onDisconnected(device: DeviceItem) {
        println("MARYSIA: AirBeamConnector onDisconnected sending event")
        EventBus.getDefault().post(SensorDisconnectedEvent(device.id, device, mSessionUUID))
        mListener?.onDisconnect(device.id)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(event: SendSessionAuth) {
        sendAuth(event.sessionUUID)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(event: ConfigureSession) {
        configureSession(event.session, event.wifiSSID, event.wifiPassword)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(event: DisconnectExternalSensorsEvent) {
        disconnect()
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(event: SensorDisconnectedEvent) {
        if (mDeviceItem?.id == event.sessionDeviceId) {
            disconnect()
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(event: StopRecordingEvent) {
        if (mSessionUUID == event.sessionUUID || mSessionUUID == null) {
            disconnect()
        }
    }

    protected fun registerToEventBus() {
        EventBus.getDefault().safeRegister(this)
    }

    protected fun unregisterFromEventBus() {
        EventBus.getDefault().unregister(this);
    }
}


