package io.lunarlogic.aircasting.sensor.airbeam3

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import io.lunarlogic.aircasting.exceptions.*
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.sensor.AirBeamConnector
import io.lunarlogic.aircasting.models.Session
import no.nordicsemi.android.ble.observer.ConnectionObserver


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
            .timeout(100000)
            .retry(3, 100)
            .done { _ -> onConnectionSuccessful(deviceItem) }
            .enqueue()
    }

    private fun bleNotSupported(): Boolean {
        val packageManager = mContext.packageManager
        return !packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
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

    override fun sync() {
        airBeam3Configurator.sync()
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {}
    override fun onDeviceConnected(device: BluetoothDevice) {}
    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        val deviceItem = DeviceItem(device)
        onConnectionFailed(deviceItem.id)
    }
    override fun onDeviceReady(device: BluetoothDevice) {}
    override fun onDeviceDisconnecting(device: BluetoothDevice) {}
    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        val deviceItem = DeviceItem(device)
        onDisconnected(deviceItem.id)
    }
}
