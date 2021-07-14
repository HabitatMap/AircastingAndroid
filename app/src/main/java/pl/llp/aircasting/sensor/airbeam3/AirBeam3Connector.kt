package pl.llp.aircasting.sensor.airbeam3

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCallback
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import no.nordicsemi.android.ble.observer.BondingObserver
import no.nordicsemi.android.ble.observer.ConnectionObserver
import pl.llp.aircasting.exceptions.BLENotSupported
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.exceptions.MissingDeviceAfterConnectionError
import pl.llp.aircasting.exceptions.SensorDisconnectedError
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.sensor.AirBeamConnector


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
        val deviceItem = DeviceItem(device)
        onConnectionFailed(deviceItem)
    }
    override fun onDeviceReady(device: BluetoothDevice) {}
    override fun onDeviceDisconnecting(device: BluetoothDevice) {}
    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        val deviceItem = DeviceItem(device)
        println("MARYSIA Airbeam3Connector onDeviceDisconnected device id ${deviceItem.id} reason ${reason}")
        mErrorHandler.handle(SensorDisconnectedError("called from Airbeam3Connector"))
        onDisconnected(deviceItem)
    }
}
