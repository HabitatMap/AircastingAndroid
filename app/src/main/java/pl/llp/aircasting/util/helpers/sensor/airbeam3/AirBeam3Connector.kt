package pl.llp.aircasting.util.helpers.sensor.airbeam3

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.util.Log.VERBOSE
import no.nordicsemi.android.ble.observer.ConnectionObserver
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.BLENotSupported
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.MissingDeviceAfterConnectionError
import pl.llp.aircasting.util.exceptions.SensorDisconnectedError
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamConnector

open class AirBeam3Connector(
    private val mContext: Context,
    settings: Settings,
    private val mErrorHandler: ErrorHandler,
    private val bluetoothManager: BluetoothManager,
    private var airBeam3Configurator: AirBeam3Configurator = AirBeam3Configurator(
        mContext,
        mErrorHandler,
        settings
    )
) : AirBeamConnector(bluetoothManager), ConnectionObserver {

    override fun start(deviceItem: DeviceItem) {
        if (bleNotSupported()) {
            throw BLENotSupported()
        }

        airBeam3Configurator.connectionObserver = this

        val bluetoothDevice = deviceItem.bluetoothDevice ?: return

        airBeam3Configurator.connect(bluetoothDevice)
            .timeout(0)
            .retry(3, 100)
            .useAutoConnect(true)
            .fail { device, status -> onFailedCallback(device, status) }
            .done { _ -> onConnectionSuccessful(deviceItem) }
            .enqueue()
    }

    private fun bleNotSupported(): Boolean {
        val packageManager = mContext.packageManager
        return !packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    private fun onFailedCallback(device: BluetoothDevice, reason: Int) {
        val deviceItem = DeviceItem(device)
        onDisconnected(deviceItem)
    }

    override fun stop() {
        airBeam3Configurator.close()
    }

    override fun configureSession(session: Session, wifiSSID: String?, wifiPassword: String?) {
        airBeam3Configurator.configure(session, wifiSSID, wifiPassword)
    }

    override fun sendAuth(sessionUUID: String) {
        airBeam3Configurator.sendAuth(sessionUUID)
    }

    override fun reconnectMobileSession() {
        airBeam3Configurator.reconnectMobileSession()
    }

    override fun triggerSDCardDownload() {
        val deviceItem = mDeviceItem
        if (deviceItem == null) {
            mErrorHandler.handle(MissingDeviceAfterConnectionError())
        } else {
            airBeam3Configurator.triggerSDCardDownload(deviceItem.id)
        }
    }

    override fun clearSDCard() {
        airBeam3Configurator.clearSDCard()
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        Log.v(TAG, "Device connecting: $device")
    }
    override fun onDeviceConnected(device: BluetoothDevice) {
        Log.v(TAG, "Device connected: $device")
    }
    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        mErrorHandler.handle(SensorDisconnectedError("called from Airbeam3Connector onDeviceFailedToConnect"))
        val deviceItem = DeviceItem(device)
        onConnectionFailed(deviceItem)
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        Log.v(TAG, "Device ready: $device")
    }
    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        mErrorHandler.handle(SensorDisconnectedError("called from Airbeam3Connector onDeviceDisconnecting"))
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        airBeam3Configurator.log(VERBOSE, "Disconnected reason: $reason")

        val deviceItem = DeviceItem(device)
        onDisconnected(deviceItem)

        airBeam3Configurator.reset()
        mErrorHandler.handle(SensorDisconnectedError("called from Airbeam3Connector onDeviceDisconnected device id ${deviceItem.id} reason ${reason}"))
        disconnect()
    }
}
