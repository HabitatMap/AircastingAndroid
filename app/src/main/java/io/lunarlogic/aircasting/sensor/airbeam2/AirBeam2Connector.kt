package io.lunarlogic.aircasting.sensor.airbeam2

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import io.lunarlogic.aircasting.bluetooth.BLEManager
import io.lunarlogic.aircasting.events.ApplicationClosed
import io.lunarlogic.aircasting.events.ConfigureSession
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.exceptions.*
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.ConnectingAirBeamController
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.sensor.Session
import no.nordicsemi.android.ble.observer.ConnectionObserver
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean


open class AirBeam2Connector(
    private val mContext: Context,
    private val mSettinngs: Settings,
    private val mErrorHandler: ErrorHandler,
    private val mAirBeamConfigurator: AirBeam2Configurator,
    private val mAirBeam2Reader: AirBeam2Reader
) {
    private val connectionStarted = AtomicBoolean(false)
    private val cancelStarted = AtomicBoolean(false)

    private var mThread: ConnectThread? = null

    private var bleManager: BLEManager? = null
    lateinit var listener: ConnectingAirBeamController.Listener

    private val scanner = BluetoothLeScannerCompat.getScanner()
    private val scanCallback = object : ScanCallback() {}

    open fun connect(deviceItem: DeviceItem) {
        if (connectionStarted.get() == false) {
            connectionStarted.set(true)
            registerToEventBus()
            mThread = ConnectThread(deviceItem)
            mThread?.start()
        }
    }

    fun cancel() {
        mThread?.cancel()
    }

    fun configureSession(session: Session, wifiSSID: String?, wifiPassword: String?) {
        mThread?.configureSession(session, wifiSSID, wifiPassword)
    }

    private inner class ConnectThread(private val deviceItem: DeviceItem) : Thread(), ConnectionObserver {
        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            stopScan()

            // TODO: inject this
            bleManager = BLEManager(mContext, mSettinngs)
            bleManager!!.setConnectionObserver(this)

            bleManager!!.connect(deviceItem.bluetoothDevice)
                .timeout(100000)
                .retry(3, 100)
                .done { _ -> listener.onConnectionSuccessful(deviceItem.id) }
                .enqueue()
        }

        private fun stopScan() {
            scanner.stopScan(scanCallback)
        }

        fun cancel() {
            unregisterFromEventBus()

            if (cancelStarted.get() == false) {
                cancelStarted.set(true)
                connectionStarted.set(false)
                bleManager?.close()
            }
        }

        fun configureSession(session: Session, wifiSSID: String?, wifiPassword: String?) {
            try {
                if (session.isFixed()) {
                    when (session.streamingMethod) {
                        Session.StreamingMethod.WIFI -> bleManager?.configureFixedWifi(session, wifiSSID!!, wifiPassword!!)
                        Session.StreamingMethod.CELLULAR -> bleManager?.configureFixedCellular(session)
                    }
                } else {
                    bleManager?.configureMobile(session)
                }
            } catch (e: IOException) {
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

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(event: ConfigureSession) {
        configureSession(event.session, event.wifiSSID, event.wifiPassword)
    }

    @Subscribe
    fun onMessageEvent(event: ApplicationClosed) {
        cancel()
    }

    @Subscribe
    fun onMessageEvent(event: StopRecordingEvent) {
        cancel()
    }

    private fun registerToEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private fun unregisterFromEventBus() {
        EventBus.getDefault().unregister(this);
    }
}
