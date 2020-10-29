package io.lunarlogic.aircasting.sensor.airbeam2

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import io.lunarlogic.aircasting.sensor.airbeam3.AirBeam3Reader
import io.lunarlogic.aircasting.exceptions.*
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.sensor.AirBeamConnector
import io.lunarlogic.aircasting.sensor.Session
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean


open class AirBeam3Connector(
    private val mContext: Context,
    private val mSettinngs: Settings,
    private val mErrorHandler: ErrorHandler
): AirBeamConnector() {
    private val connectionStarted = AtomicBoolean(false)
    private val cancelStarted = AtomicBoolean(false)

    private var mThread: ConnectThread? = null

    private var airBeam3Reader: AirBeam3Reader? = null

    override fun connect(deviceItem: DeviceItem) {
        if (connectionStarted.get() == false) {
            connectionStarted.set(true)
            registerToEventBus()
            mThread = ConnectThread(deviceItem)
            mThread?.start()
        }
    }

    override fun disconnect() {
        mThread?.cancel()
    }

    override fun configureSession(session: Session, wifiSSID: String?, wifiPassword: String?) {
        mThread?.configureSession(session, wifiSSID, wifiPassword)
    }

    override fun sendAuth(uuid: String) {
        mThread?.sendAuth(uuid)
    }

    // TODO: actually this thread can be not needed here as we already use async connection method - enqueue
    private inner class ConnectThread(private val deviceItem: DeviceItem) : Thread(), ConnectionObserver {
        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            val bluetoothAdapter =  BluetoothAdapter.getDefaultAdapter()
            bluetoothAdapter?.cancelDiscovery()

            // TODO: inject this
            airBeam3Reader = AirBeam3Reader(mContext, mErrorHandler, mSettinngs)
            airBeam3Reader!!.setConnectionObserver(this)

            airBeam3Reader!!.connect(deviceItem.bluetoothDevice)
                .timeout(100000)
                .retry(3, 100)
                .done { _ -> onConnectionSuccessful(deviceItem.id) }
                .enqueue()
        }

        fun cancel() {
            unregisterFromEventBus()

            if (cancelStarted.get() == false) {
                cancelStarted.set(true)
                connectionStarted.set(false)
                airBeam3Reader?.close()
                cancelStarted.set(false)
            }
        }

        fun sendAuth(uuid: String) {
            airBeam3Reader?.sendAuth(uuid)
        }

        fun configureSession(session: Session, wifiSSID: String?, wifiPassword: String?) {
            try {
                if (session.isFixed()) {
                    val location = session.location!! // TODO: handle !! in a better way

                    when (session.streamingMethod) {
                        Session.StreamingMethod.WIFI -> airBeam3Reader?.configureFixedWifi(location, wifiSSID!!, wifiPassword!!)
                        Session.StreamingMethod.CELLULAR -> airBeam3Reader?.configureFixedCellular(location)
                    }
                } else {
                    airBeam3Reader?.configureMobile()
                }
            } catch (e: IOException) {
                // TODO: is it really thrown for BLE?
                mErrorHandler.handle(AirBeam2ConfiguringFailed(e))
            }
        }

        override fun onDeviceConnecting(device: BluetoothDevice) {}

        override fun onDeviceConnected(device: BluetoothDevice) {}

        override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {}

        override fun onDeviceReady(device: BluetoothDevice) {}

        override fun onDeviceDisconnecting(device: BluetoothDevice) {}

        override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {}
    }
}
