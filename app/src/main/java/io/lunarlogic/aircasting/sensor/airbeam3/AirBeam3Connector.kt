package io.lunarlogic.aircasting.sensor.airbeam2

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import io.lunarlogic.aircasting.exceptions.*
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.sensor.AirBeamConnector
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.sensor.airbeam3.AirBeam3Configurator
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

        airBeam3Configurator.connect(deviceItem.bluetoothDevice)
            .timeout(100000)
            .retry(3, 100)
            .done { _ -> onConnectionSuccessful(deviceItem.id) }
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

    override fun sendAuth(uuid: String) {
        airBeam3Configurator.sendAuth(uuid)
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {}
    override fun onDeviceConnected(device: BluetoothDevice) {}
    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {}
    override fun onDeviceReady(device: BluetoothDevice) {}
    override fun onDeviceDisconnecting(device: BluetoothDevice) {}
    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {}
}
