package pl.llp.aircasting.util.helpers.sensor.airbeam3

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import no.nordicsemi.android.ble.observer.ConnectionObserver
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.BLENotSupported
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.MissingDeviceAfterConnectionError
import pl.llp.aircasting.util.exceptions.SensorDisconnectedError
import pl.llp.aircasting.util.helpers.sensor.AirBeamConnector

open class AirBeam3Connector(
    private val mContext: Context,
    mSettinngs: Settings,
    private val mErrorHandler: ErrorHandler
): AirBeamConnector(), ConnectionObserver {
    private var airBeam3Configurator = AirBeam3Configurator(mContext, mErrorHandler, mSettinngs)

    override fun start(deviceItem: DeviceItem) {
        if (bleNotSupported()) {
            throw BLENotSupported()
        }

        airBeam3Configurator.setConnectionObserver(this)

        val bluetoothDevice = deviceItem.bluetoothDevice ?: return

        airBeam3Configurator.connect(bluetoothDevice)
            .timeout(0)
            .retry(3, 100)
            .fail { device, status ->  onFailedCallback(device, status)}
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

    override fun onDeviceConnecting(device: BluetoothDevice) {}
    override fun onDeviceConnected(device: BluetoothDevice) {}
    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        mErrorHandler.handle(SensorDisconnectedError("called from Airbeam3Connector onDeviceFailedToConnect"))
        val deviceItem = DeviceItem(device)
        onConnectionFailed(deviceItem)
    }
    override fun onDeviceReady(device: BluetoothDevice) {}
    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        mErrorHandler.handle(SensorDisconnectedError("called from Airbeam3Connector onDeviceDisconnecting"))
    }
    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        val deviceItem = DeviceItem(device)

        mErrorHandler.handle(SensorDisconnectedError("called from Airbeam3Connector onDeviceDisconnected device id ${deviceItem.id} reason ${reason}"))
        onDisconnected(deviceItem)
        disconnect()
    }
}