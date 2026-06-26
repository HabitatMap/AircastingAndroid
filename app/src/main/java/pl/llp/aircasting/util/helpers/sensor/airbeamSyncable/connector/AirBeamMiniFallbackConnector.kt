package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.connector

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.util.Log.VERBOSE
import no.nordicsemi.android.ble.observer.ConnectionObserver
import no.nordicsemi.android.ble.observer.ConnectionObserver.REASON_TERMINATE_PEER_USER
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.data.api.services.FixedSessionConfig
import pl.llp.aircasting.util.exceptions.BLENotSupported
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SensorDisconnectedError
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamBleConfigurator
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2Configurator
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import pl.llp.aircasting.util.helpers.sensor.common.connector.AirBeamConnector

class AirBeamMiniFallbackConnector(
    private val applicationContext: Context,
    private val mErrorHandler: ErrorHandler,
    bluetoothManager: BluetoothManager,
    private val v2Configurator: AirBeamMiniV2Configurator,
    private val v1Configurator: AirBeamBleConfigurator,
    private val v2StateRepository: AirBeamMiniV2StateRepository,
) : AirBeamConnector(bluetoothManager), ConnectionObserver {

    private var activeConfigurator: AirBeamBleConfigurator = v2Configurator
    private var isV2Attempt = true
    private var currentDeviceItem: DeviceItem? = null

    // Nordic BleManager fires both `ConnectRequest.fail` and
    // `ConnectionObserver.onDeviceFailedToConnect` for the same failure event (e.g.
    // service-not-supported on V2 against a V1 device). Without idempotency the second
    // callback runs after `isV2Attempt` has flipped to false and wrongly enters the
    // "V1 also failed" branch — posting AirBeamConnectionFailedEvent and tearing down
    // the in-flight V1 connect started by the first callback. Track per-attempt
    // handling so only the first failure callback for each leg drives state.
    private var v2FailureHandled = false
    private var v1FailureHandled = false

    override fun start(deviceItem: DeviceItem) {
        if (bleNotSupported()) throw BLENotSupported()

        currentDeviceItem = deviceItem
        isV2Attempt = true
        activeConfigurator = v2Configurator
        v2FailureHandled = false
        v1FailureHandled = false
        Log.d("[RECONNECT]", "AirBeamMiniFallback.start: attempting V2 first (device=${deviceItem.id} address=${deviceItem.address})")
        connectWith(v2Configurator, deviceItem)
    }

    private fun connectWith(configurator: AirBeamBleConfigurator, deviceItem: DeviceItem) {
        configurator.setObserver(this)
        val bluetoothDevice = deviceItem.bluetoothDevice
            ?: deviceItem.address.takeIf { it.isNotEmpty() }
                ?.let { BluetoothAdapter.getDefaultAdapter()?.getRemoteDevice(it) }
            ?: run {
                Log.w("[RECONNECT]", "AirBeamMiniFallback.connectWith: no BluetoothDevice (device=${deviceItem.id} address=${deviceItem.address})")
                return
            }

        // Capture leg at closure-creation time. Nordic's `.fail` callback for the V2
        // ConnectRequest can fire AFTER `onDeviceDisconnected` has already routed the
        // failure through `onFailedCallback` and flipped `isV2Attempt` to false. Reading
        // `isV2Attempt` at callback time would misclassify the late V2 `.fail` as a V1
        // failure and post `AirBeamConnectionFailedEvent`.
        val isV2Leg = isV2Attempt
        Log.d("[RECONNECT]", "AirBeamMiniFallback.connectWith: ${if (isV2Leg) "V2" else "V1"} device=${deviceItem.id} address=${bluetoothDevice.address} autoConnect=true")
        configurator.connectDevice(bluetoothDevice)
            .timeout(0)
            .useAutoConnect(true)
            .fail { device, status -> onFailedCallback(device, status, fromV2Leg = isV2Leg) }
            .done { _ ->
                Log.d(TAG, "AirBeamMiniFallback: Connected with ${if (isV2Leg) "V2" else "V1"}")
                val effectiveDeviceItem = if (isV2Leg) {
                    v2Configurator.deviceId = deviceItem.id
                    DeviceItem(
                        deviceItem.bluetoothDevice,
                        deviceItem.name,
                        deviceItem.address,
                        deviceItem.id,
                        deviceItem.type,
                        DeviceItem.FirmwareVersion.V2,
                    )
                } else {
                    deviceItem
                }
                onConnectionSuccessful(effectiveDeviceItem)
            }
            .enqueue()
    }

    private fun onFailedCallback(device: BluetoothDevice, reason: Int, fromV2Leg: Boolean) {
        if (fromV2Leg) {
            if (v2FailureHandled) {
                Log.d("[RECONNECT]", "AirBeamMiniFallback.onFailedCallback: V2 leg failure already handled (reason=$reason) — ignoring duplicate")
                return
            }
            v2FailureHandled = true
            Log.w("[RECONNECT]", "AirBeamMiniFallback.onFailedCallback: V2 failed (reason=$reason), falling back to V1 device=${device.address}")
            isV2Attempt = false
            v2Configurator.closeConnection()
            v2Configurator.reset()
            activeConfigurator = v1Configurator
            val deviceItem = currentDeviceItem ?: DeviceItem(device)
            connectWith(v1Configurator, deviceItem)
        } else {
            if (v1FailureHandled) {
                Log.d("[RECONNECT]", "AirBeamMiniFallback.onFailedCallback: V1 leg failure already handled (reason=$reason) — ignoring duplicate")
                return
            }
            v1FailureHandled = true
            Log.w("[RECONNECT]", "AirBeamMiniFallback.onFailedCallback: V1 also failed (reason=$reason) device=${device.address} — posting connection-failed for retry loop")
            // This is a connection *failure* (Nordic .fail on connectDevice), not a
            // post-connect link loss. Route through onConnectionFailed so
            // AirBeamReconnector's retry loop fires AirBeamConnectionFailedEvent
            // and schedules the next attempt; onDisconnected would instead post
            // SensorDisconnectedUnexpectedlyEvent which AirBeamService's handler
            // would forward to tryToReconnectPeriodically — which rejects with
            // "Reconnection already in progress" since mReconnectionTriesNumber
            // is already set, silently stalling the loop.
            val deviceItem = currentDeviceItem ?: DeviceItem(device)
            onConnectionFailed(deviceItem)
        }
    }

    private fun bleNotSupported(): Boolean {
        val packageManager = applicationContext.packageManager
        return !packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    override fun stop() {
        Log.d("[RECONNECT]", "AirBeamMiniFallback.stop -> closing both configurators")
        v2Configurator.closeConnection()
        v1Configurator.closeConnection()
    }

    override fun configureSession(session: Session, wifiSSID: String?, wifiPassword: String?, fixedSessionConfig: FixedSessionConfig?, intervalSeconds: Int?) {
        activeConfigurator.configure(session, wifiSSID, wifiPassword, fixedSessionConfig, intervalSeconds)
    }

    override fun sendAuth(sessionUUID: String) {
        activeConfigurator.sendAuth(sessionUUID)
    }

    override fun reconnectMobileSession() {
        activeConfigurator.reconnectMobileSession()
    }

    override fun triggerSDCardDownload() {
        activeConfigurator.triggerSDCardDownload()
    }

    override suspend fun clearSDCard() {
        activeConfigurator.clearSDCard()
    }

    override fun discardSession() {
        activeConfigurator.discardSession()
    }

    fun isV2Connected(): Boolean = isV2Attempt && connectionEstablished.get()

    // -- ConnectionObserver --

    override fun onDeviceConnecting(device: BluetoothDevice) {
        Log.v(TAG, "AirBeamMiniFallback: Device connecting: $device")
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        Log.v(TAG, "AirBeamMiniFallback: Device connected: $device")
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        Log.w("[RECONNECT]", "AirBeamMiniFallback.onDeviceFailedToConnect device=${device.address} reason=$reason isV2Attempt=$isV2Attempt")
        // Observer callbacks aren't request-scoped, so we don't have a captured leg.
        // Map by current isV2Attempt — onFailedCallback's dedup flag ensures duplicate
        // V2 callbacks after the transition are ignored.
        onFailedCallback(device, reason, fromV2Leg = isV2Attempt)
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        Log.v(TAG, "AirBeamMiniFallback: Device ready (${if (isV2Attempt) "V2" else "V1"}): $device")
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        mErrorHandler.handle(SensorDisconnectedError("AirBeamMiniFallback onDeviceDisconnecting"))
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        activeConfigurator.log(VERBOSE, "Disconnected reason: $reason")
        Log.w("[RECONNECT]", "AirBeamMiniFallback.onDeviceDisconnected device=${device.address} reason=$reason isV2=$isV2Attempt syncInProgress=${v2StateRepository.syncInProgress} established=${connectionEstablished.get()}")

        val deviceItem = DeviceItem(device)

        // V2 manual sync: if BLE drops mid-HTTP-download (BLE+Wi-Fi coex symptom on Android
        // 12 + ESP32), let the orchestrator finish parsing whatever it already received over
        // the SoftAP. Skip reset/disconnect here; the orchestrator's launch block in
        // `AirBeamSyncService.onConnectionSuccessful` calls `airBeamConnector.disconnect()`
        // and the configurator is reset by the next connect.
        if (isV2Attempt && v2StateRepository.syncInProgress) {
            Log.d("[RECONNECT]", "AirBeamMiniFallback: BLE disconnected during V2 sync (reason=$reason) — deferring teardown until orchestrator finishes")
            mErrorHandler.handle(SensorDisconnectedError("AirBeamMiniFallback onDeviceDisconnected during V2 sync, deferring (reason=$reason)"))
            return
        }

        // V2 service-not-supported (and any other pre-success V2 disconnect) on a V1
        // firmware device: Nordic surfaces this via onDeviceDisconnected — NOT
        // onDeviceFailedToConnect. If we fall through to the standard teardown here,
        // `onDisconnected()` posts SensorDisconnectedUnexpectedlyEvent → AirBeamService
        // stopSelf()'s AirBeamRecordSessionService, and `disconnect()` unregisters
        // AirBeamConnector from EventBus. The subsequent .fail callback then transitions
        // to V1 and V1 connects successfully — but the service is dead, so ConfigureSession
        // has no subscriber when the user taps Start Recording. Route this through the
        // idempotent fallback path instead. The dedup flag prevents re-entry when .fail
        // also fires.
        if (isV2Attempt && !connectionEstablished.get()) {
            Log.d("[RECONNECT]", "AirBeamMiniFallback: V2 disconnected before success (reason=$reason) — routing to V1 fallback without teardown")
            onFailedCallback(device, reason, fromV2Leg = true)
            return
        }

        onDisconnected(deviceItem, isDisconnectedUnexpectedly = reason != REASON_TERMINATE_PEER_USER)

        activeConfigurator.reset()
        mErrorHandler.handle(SensorDisconnectedError("AirBeamMiniFallback onDeviceDisconnected device id ${deviceItem.id} reason $reason"))
        disconnect()
    }
}
