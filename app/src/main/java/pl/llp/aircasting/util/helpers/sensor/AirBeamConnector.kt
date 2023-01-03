package pl.llp.aircasting.util.helpers.sensor

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.events.*
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.timerTask

abstract class AirBeamConnector(
    private val bluetoothManager: BluetoothManager? = null
) {
    interface Listener {
        fun onConnectionSuccessful(deviceItem: DeviceItem, sessionUUID: String?)
        fun onConnectionFailed(deviceItem: DeviceItem)
        fun onDisconnect(deviceId: String)
    }

    private var mListener: Listener? = null
    private val CONNECTION_TIMEOUT = 30000L

    protected val connectionStarted = AtomicBoolean(false)
    protected val cancelStarted = AtomicBoolean(false)
    protected val connectionEstablished = AtomicBoolean(false)
    protected val connectionTimedOut = AtomicBoolean(false)

    protected var mDeviceItem: DeviceItem? = null
    protected var mSessionUUID: String? = null

    protected abstract fun start(deviceItem: DeviceItem)
    protected abstract fun stop()
    protected abstract fun sendAuth(sessionUUID: String)
    protected abstract fun configureSession(session: Session, wifiSSID: String?, wifiPassword: String?)

    fun connect(deviceItem: DeviceItem, sessionUUID: String? = null) {
        mDeviceItem = deviceItem
        mSessionUUID = sessionUUID

        connectionTimedOut.set(false)

        // Cancel discovery because it otherwise slows down the connection.
        bluetoothManager?.cancelDiscovery()

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
        mDeviceItem = deviceItem
        connectionEstablished.set(true)
        mListener?.onConnectionSuccessful(deviceItem, mSessionUUID)
    }

    fun onConnectionFailed(deviceItem: DeviceItem) {
        mTimerTask?.cancel()
        if (connectionTimedOut.get() == false) {
            mListener?.onConnectionFailed(deviceItem)
        }
    }

    fun onDisconnected(device: DeviceItem, postDisconnectedEvent: Boolean = true) {
        if (postDisconnectedEvent) EventBus.getDefault().post(SensorDisconnectedEvent(device.id, device, mSessionUUID))
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
        EventBus.getDefault().unregister(this)
    }
}
