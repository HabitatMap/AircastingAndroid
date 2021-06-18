package pl.llp.aircasting.sensor

import android.bluetooth.BluetoothAdapter
import pl.llp.aircasting.events.*
import pl.llp.aircasting.lib.safeRegister
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.timerTask

abstract class AirBeamConnector {
    interface Listener {
        fun onConnectionSuccessful(deviceItem: DeviceItem, sessionUUID: String?)
        fun onConnectionFailed(deviceId: String)
        fun onDisconnect(deviceId: String)
    }

    private var mListener: Listener? = null
    private val CONNECTION_TIMEOUT = 10000L

    protected val connectionStarted = AtomicBoolean(false)
    protected val cancelStarted = AtomicBoolean(false)
    protected val connectionEstablished = AtomicBoolean(false)
    protected val connectionTimedOut = AtomicBoolean(false)

//    private var reconnectionTriesNumber: Int? = null
//    protected val reconnectionStarted = AtomicBoolean(false)
//    private val RECONNECTION_TRIES_MAX = 5

    protected var mDeviceItem: DeviceItem? = null
    protected var mSessionUUID: String? = null

    abstract protected fun start(deviceItem: DeviceItem)
    abstract protected fun stop()
    abstract protected fun sendAuth(sessionUUID: String)
    abstract protected fun configureSession(session: Session, wifiSSID: String?, wifiPassword: String?)

    fun connect(deviceItem: DeviceItem, sessionUUID: String? = null) {
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
                mListener?.onConnectionFailed(deviceItem.id)
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
        mDeviceItem = deviceItem
        connectionEstablished.set(true)
        mListener?.onConnectionSuccessful(deviceItem, mSessionUUID)
    }

    fun onConnectionFailed(deviceId: String) {
        mTimerTask?.cancel()
        if (connectionTimedOut.get() == false) {
            mListener?.onConnectionFailed(deviceId)
        }
    }

    fun onDisconnected(deviceId: String) {
        EventBus.getDefault().post(SensorDisconnectedEvent(deviceId, mSessionUUID))
        mListener?.onDisconnect(deviceId)
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
        println("MARYSIA: DisconnectExternalSensorsEvent received")
        disconnect()
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(event: SensorDisconnectedEvent) {
        println("MARYSIA: SensorDisconnectedEvent received")
        // IF WE ARE HERE IT MEANS SOME DISCONNECTION HAPPENED
        if (mDeviceItem?.id == event.deviceId) {
            disconnect()
        }
    }

    fun reconnect() {
        if (mDeviceItem == null || mSessionUUID == null) return

        connect(mDeviceItem!!, mSessionUUID!!)
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
