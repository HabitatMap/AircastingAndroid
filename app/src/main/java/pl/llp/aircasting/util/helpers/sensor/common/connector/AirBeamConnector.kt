package pl.llp.aircasting.util.helpers.sensor.common.connector

import android.util.Log
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.data.api.services.FixedSessionConfig
import pl.llp.aircasting.util.events.ConfigureSession
import pl.llp.aircasting.util.events.DisconnectExternalSensorsEvent
import pl.llp.aircasting.util.events.SendSessionAuth
import pl.llp.aircasting.util.events.SensorDisconnectedUnexpectedlyEvent
import pl.llp.aircasting.util.events.StopRecordingEvent
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.timerTask

abstract class AirBeamConnector(
    private val bluetoothManager: BluetoothManager? = null,
    private val deviceAddressByDeviceItem: MutableMap<String, DeviceItem> = ConcurrentHashMap()
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
    protected abstract fun configureSession(
        session: Session,
        wifiSSID: String?,
        wifiPassword: String?,
        fixedSessionConfig: FixedSessionConfig? = null,
        intervalSeconds: Int? = null,
    )

    fun connect(deviceItem: DeviceItem, sessionUUID: String? = null) {
        mDeviceItem = deviceItem
        mSessionUUID = sessionUUID

        connectionTimedOut.set(false)

        // Cancel discovery because it otherwise slows down the connection.
        Log.d(TAG, "Cancelling discovery with bluetooth manager: $bluetoothManager")
        bluetoothManager?.cancelDiscovery()

        if (!connectionStarted.get()) {
            Log.d(TAG, "Connection started")
            failAfterTimeout(deviceItem)
            connectionStarted.set(true)
            registerToEventBus()
            start(deviceItem)
        }
    }

    abstract fun reconnectMobileSession()
    abstract fun triggerSDCardDownload()
    abstract suspend fun clearSDCard()

    private var mTimerTask: TimerTask? = null

    private fun failAfterTimeout(deviceItem: DeviceItem) {
        mTimerTask = timerTask {
            if (!connectionEstablished.get()) {
                connectionTimedOut.set(true)
                mListener?.onConnectionFailed(deviceItem)
            }
        }
        Timer().schedule(mTimerTask, CONNECTION_TIMEOUT)
    }

    fun disconnect() {
        Log.d("[FG-DEBUG]", "AirBeamConnector.disconnect() entered (cancelStarted=${cancelStarted.get()})")
        unregisterFromEventBus()

        mTimerTask?.cancel()

        if (!cancelStarted.get()) {
            cancelStarted.set(true)
            connectionStarted.set(false)
            Log.d("[FG-DEBUG]", "AirBeamConnector.disconnect() calling stop()")
            stop()
            cancelStarted.set(false)
        } else {
            Log.d("[FG-DEBUG]", "AirBeamConnector.disconnect() SKIPPED stop() — cancelStarted already true")
        }
    }

    fun registerListener(listener: Listener) {
        mListener = listener
    }

    fun onConnectionSuccessful(deviceItem: DeviceItem) {
        mDeviceItem = deviceItem
        deviceAddressByDeviceItem[deviceItem.address] = deviceItem
        connectionEstablished.set(true)
        mTimerTask?.cancel()
        mListener?.onConnectionSuccessful(deviceItem, mSessionUUID)
    }

    fun onConnectionFailed(deviceItem: DeviceItem) {
        mTimerTask?.cancel()
        if (!connectionTimedOut.get()) {
            mListener?.onConnectionFailed(deviceItem)
        }
    }

    fun onDisconnected(device: DeviceItem, isDisconnectedUnexpectedly: Boolean = true) {
        // Always prefer the DeviceItem captured at connection time (keyed by MAC). The
        // BluetoothDevice surfaced to onDeviceDisconnected may report a different `name`
        // than the user picked from scan results — V2 / NimBLE returns the GAP "Device
        // Name" ("nimble" by default) instead of the advertised name, and DeviceItem.id
        // is derived from that name. A freshly-built DeviceItem here yields id="nimble",
        // which fails to match `session.deviceId` (stored at session-create time from the
        // picker) and silently breaks (1) auto-reconnect (`session.deviceId ==
        // event.sessionDeviceId` is false in AirBeamService.onMessageEvent) and (2) the
        // disconnected-card UI (RecordingHandler.disconnectSession scopes by deviceId, so
        // session.status stays RECORDING and `session.isDisconnected()` returns false).
        val deviceItem = deviceAddressByDeviceItem[device.address] ?: device

        if (isDisconnectedUnexpectedly) {
            Log.d(TAG, "Posting SensorDisconnectedUnexpectedlyEvent")
            EventBus.getDefault()
                .post(SensorDisconnectedUnexpectedlyEvent(deviceItem.id, deviceItem, mSessionUUID))
        }
        Log.d("[FG-DEBUG]", "AirBeamConnector.onDisconnected() invoking listener.onDisconnect (listener=${mListener?.javaClass?.simpleName}, unexpected=$isDisconnectedUnexpectedly)")
        mListener?.onDisconnect(deviceItem.id)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(event: SendSessionAuth) {
        sendAuth(event.sessionUUID)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(event: ConfigureSession) {
        configureSession(event.session, event.wifiSSID, event.wifiPassword, event.fixedSessionConfig, event.intervalSeconds)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(event: DisconnectExternalSensorsEvent) {
        Log.d("[FG-DEBUG]", "AirBeamConnector received DisconnectExternalSensorsEvent")
        disconnect()
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(event: SensorDisconnectedUnexpectedlyEvent) {
        if (mDeviceItem?.id == event.sessionDeviceId) {
            disconnect()
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(event: StopRecordingEvent) {
        if (mSessionUUID == event.sessionUUID || mSessionUUID == null) {
            discardSession()
            disconnect()
        }
    }

    protected open fun discardSession() {
        // no-op by default; V2 overrides to send DiscardSession (0x11)
    }

    protected fun registerToEventBus() {
        EventBus.getDefault().safeRegister(this)
    }

    protected fun unregisterFromEventBus() {
        EventBus.getDefault().unregister(this)
    }
}
