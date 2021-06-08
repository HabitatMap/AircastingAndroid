package io.lunarlogic.aircasting.sensor

import android.bluetooth.BluetoothAdapter
import io.lunarlogic.aircasting.events.*
import io.lunarlogic.aircasting.lib.safeRegister
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
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
    protected val reconnectionStarted = AtomicBoolean(false)
    protected val reconnectionSuccessful = AtomicBoolean(false)

    private var reconnectionTriesNumber = 0

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

    fun reconnect() {
        // check if they are set properly
        println("MARYSIA: reconnect: mDeviceItem ${mDeviceItem} mSessionUUID ${mSessionUUID}")
        if (mDeviceItem == null || mSessionUUID == null) return

        connect(mDeviceItem!!, mSessionUUID!!)
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

    private fun disconnect() {
        unregisterFromEventBus()

        if (!cancelStarted.get()) {
            cancelStarted.set(true)
            connectionStarted.set(false)
            stop()
            cancelStarted.set(false)
        }
    }

    private fun tryReconnect(tryNumber: Int = 1) {
        if (tryNumber == 1) {
            if (reconnectionStarted.get()) {
                return
            } else {
                reconnectionStarted.set(true)
            }
        }

        reconnectionTriesNumber  = tryNumber
        println("MARYSIA: trying to reconect")
        if (tryNumber > 5) {
            onDisconnected(mDeviceItem!!.id)
            return
        }


        reconnectionSuccessful.set(false)
        connectionStarted.set(false)
        reconnect()


    }

    fun registerListener(listener: Listener) {
        mListener = listener
    }

    fun onConnectionSuccessful(deviceItem: DeviceItem) {
        mDeviceItem = deviceItem
        connectionEstablished.set(true)
        if (reconnectionStarted.get()) {
            reconnectionSuccessful.set(true)
        }
        mListener?.onConnectionSuccessful(deviceItem, mSessionUUID)
    }

    fun onConnectionFailed(deviceId: String) {
        if (reconnectionStarted.get()) {
            tryReconnect(reconnectionTriesNumber + 1)
            return
        }
        mTimerTask?.cancel()
        if (connectionTimedOut.get() == false) {
            mListener?.onConnectionFailed(deviceId)
        }
    }

    fun onDisconnected(deviceId: String) {
        if (mDeviceItem?.id == deviceId) {

            println("MARYSIA: onDisconnected, trying to RECONNECT")
            if (reconnectionStarted.get()) {
                // if we got here it means reconnection failed 5 times and we proceed with disconnect
                EventBus.getDefault().post(SensorDisconnectedEvent(deviceId))
                mListener?.onDisconnect(deviceId)
                reconnectionStarted.set(false)
            } else {
                tryReconnect()
            }
        }

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
        println("MARYSIA: DISCONNECTED")

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
