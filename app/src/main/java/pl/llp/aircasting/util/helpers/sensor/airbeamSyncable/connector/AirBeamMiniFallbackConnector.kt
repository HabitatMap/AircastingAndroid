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
import pl.llp.aircasting.util.helpers.sensor.common.connector.AirBeamConnector

class AirBeamMiniFallbackConnector(
    private val applicationContext: Context,
    private val mErrorHandler: ErrorHandler,
    bluetoothManager: BluetoothManager,
    private val v2Configurator: AirBeamMiniV2Configurator,
    private val v1Configurator: AirBeamBleConfigurator,
) : AirBeamConnector(bluetoothManager), ConnectionObserver {

    private var activeConfigurator: AirBeamBleConfigurator = v2Configurator
    private var isV2Attempt = true
    private var currentDeviceItem: DeviceItem? = null

    override fun start(deviceItem: DeviceItem) {
        if (bleNotSupported()) throw BLENotSupported()

        currentDeviceItem = deviceItem
        isV2Attempt = true
        activeConfigurator = v2Configurator
        Log.d(TAG, "AirBeamMiniFallback: Attempting V2 connection first")
        connectWith(v2Configurator, deviceItem)
    }

    private fun connectWith(configurator: AirBeamBleConfigurator, deviceItem: DeviceItem) {
        configurator.setObserver(this)
        val bluetoothDevice = deviceItem.bluetoothDevice
            ?: deviceItem.address.takeIf { it.isNotEmpty() }
                ?.let { BluetoothAdapter.getDefaultAdapter()?.getRemoteDevice(it) }
            ?: return

        configurator.connectDevice(bluetoothDevice)
            .timeout(0)
            .useAutoConnect(true)
            .fail { device, status -> onFailedCallback(device, status) }
            .done { _ ->
                Log.d(TAG, "AirBeamMiniFallback: Connected with ${if (isV2Attempt) "V2" else "V1"}")
                val effectiveDeviceItem = if (isV2Attempt) {
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

    private fun onFailedCallback(device: BluetoothDevice, reason: Int) {
        if (isV2Attempt) {
            Log.d(TAG, "AirBeamMiniFallback: V2 failed (reason=$reason), falling back to V1")
            isV2Attempt = false
            v2Configurator.closeConnection()
            activeConfigurator = v1Configurator
            val deviceItem = currentDeviceItem ?: DeviceItem(device)
            connectWith(v1Configurator, deviceItem)
        } else {
            val deviceItem = DeviceItem(device)
            onDisconnected(deviceItem)
        }
    }

    private fun bleNotSupported(): Boolean {
        val packageManager = applicationContext.packageManager
        return !packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    override fun stop() {
        activeConfigurator.closeConnection()
    }

    override fun configureSession(session: Session, wifiSSID: String?, wifiPassword: String?, fixedSessionConfig: FixedSessionConfig?) {
        activeConfigurator.configure(session, wifiSSID, wifiPassword, fixedSessionConfig)
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
        if (isV2Attempt) {
            Log.d(TAG, "AirBeamMiniFallback: V2 device failed to connect, trying V1")
            onFailedCallback(device, reason)
        } else {
            mErrorHandler.handle(SensorDisconnectedError("AirBeamMiniFallback: Both V2 and V1 failed"))
            onConnectionFailed(DeviceItem(device))
        }
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        Log.v(TAG, "AirBeamMiniFallback: Device ready (${if (isV2Attempt) "V2" else "V1"}): $device")
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        mErrorHandler.handle(SensorDisconnectedError("AirBeamMiniFallback onDeviceDisconnecting"))
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        activeConfigurator.log(VERBOSE, "Disconnected reason: $reason")

        val deviceItem = DeviceItem(device)
        onDisconnected(deviceItem, isDisconnectedUnexpectedly = reason != REASON_TERMINATE_PEER_USER)

        activeConfigurator.reset()
        mErrorHandler.handle(SensorDisconnectedError("AirBeamMiniFallback onDeviceDisconnected device id ${deviceItem.id} reason $reason"))
        disconnect()
    }
}
