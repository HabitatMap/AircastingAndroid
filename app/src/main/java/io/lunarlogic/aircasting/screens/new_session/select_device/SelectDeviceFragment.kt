package io.lunarlogic.aircasting.screens.new_session.select_device

import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.bluetooth.BLEManager
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import no.nordicsemi.android.ble.observer.ConnectionObserver
import no.nordicsemi.android.support.v18.scanner.*
import java.util.concurrent.atomic.AtomicBoolean


class SelectDeviceFragment() : Fragment(), ConnectionObserver {
    private var controller: SelectDeviceController? = null
    var listener: SelectDeviceViewMvc.Listener? = null
    var bluetoothManager: BluetoothManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            SelectDeviceViewMvcImpl(
                layoutInflater,
                null
            )
        if (bluetoothManager != null && listener != null) {
            controller =
                SelectDeviceController(
                    context,
                    view,
                    bluetoothManager!!,
                    listener!!
                )
        }

        return view.rootView
    }

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)


//    0000ffe0-0000-1000-8000-00805f9b34fb C - nie dziala
//    0000ffe1-0000-1000-8000-00805f9b34fb F
//    0000ffe2-0000-1000-8000-00805f9b34fb K - nie dziala
//    0000ffe3-0000-1000-8000-00805f9b34fb Humidity
//    0000ffe4-0000-1000-8000-00805f9b34fb PM1
//    0000ffe5-0000-1000-8000-00805f9b34fb PM2.5
//    0000ffe6-0000-1000-8000-00805f9b34fb PM10


    private val connectionStarted = AtomicBoolean(false)

    private var bleManager: BLEManager? = null

    override fun onStart() {
        super.onStart()

        activity?.packageManager?.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            // TODO: handle BLE not supported
        }

        bleManager = BLEManager(requireContext())
        bleManager!!.setConnectionObserver(this)

        val scanner = BluetoothLeScannerCompat.getScanner()
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device

                if (device.name == "AirBeam3:246f28c47698") {
                    bleManager!!.connect(device)
                        .timeout(100000)
                        .retry(3, 100)
                        .done { device -> Log.i(BLEManager.TAG, "Device initiated") }
                        .enqueue()
                }
            }
        }
        scanner.startScan(scanCallback)

//        controller?.onStart()
    }

    override fun onStop() {
        super.onStop()
//        controller?.onStop()
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        Log.i(BLEManager.TAG, "onDeviceConnecting")
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        Log.i(BLEManager.TAG, "onDeviceConnected")
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        Log.i(BLEManager.TAG, "onDeviceFailedToConnect")
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        Log.i(BLEManager.TAG, "onDeviceReady")
        bleManager!!.run()
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        Log.i(BLEManager.TAG, "onDeviceDisconnecting")
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        Log.i(BLEManager.TAG, "onDeviceDisconnected")
    }
}
