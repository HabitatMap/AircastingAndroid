package io.lunarlogic.aircasting.sensor

import android.bluetooth.BluetoothAdapter
import io.lunarlogic.aircasting.events.*
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.atomic.AtomicBoolean

abstract class AirBeamConnector {
    interface Listener {
        fun onConnectionSuccessful(deviceItem: DeviceItem)
        fun onConnectionFailed(deviceId: String)
    }

    private var mListener: Listener? = null

    protected val connectionStarted = AtomicBoolean(false)
    protected val cancelStarted = AtomicBoolean(false)

    private var mDeviceItem: DeviceItem? = null

    abstract protected fun start(deviceItem: DeviceItem)
    abstract protected fun stop()
    abstract protected fun sendAuth(sessionUUID: String)
    abstract protected fun configureSession(session: Session, wifiSSID: String?, wifiPassword: String?)

    fun connect(deviceItem: DeviceItem) {
        mDeviceItem = deviceItem

        // Cancel discovery because it otherwise slows down the connection.
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter?.cancelDiscovery()

        if (!connectionStarted.get()) {
            connectionStarted.set(true)
            registerToEventBus()
            start(deviceItem)
        }
    }

    abstract fun reconnectMobileSession()

    private fun disconnect() {
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
        mListener?.onConnectionSuccessful(deviceItem)
    }

    fun onConnectionFailed(deviceId: String) {
        mListener?.onConnectionFailed(deviceId)
    }

    fun onDisconnected(deviceId: String) {
        EventBus.getDefault().post(SensorDisconnectedEvent(deviceId))
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
        if (mDeviceItem?.id == event.deviceId) {
            disconnect()
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(event: StopRecordingEvent) {
        disconnect()
    }

    protected fun registerToEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    protected fun unregisterFromEventBus() {
        EventBus.getDefault().unregister(this);
    }
}
