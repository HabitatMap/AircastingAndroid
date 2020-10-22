package io.lunarlogic.aircasting.screens.new_session.select_device

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import io.lunarlogic.aircasting.bluetooth.BLEManager
import no.nordicsemi.android.ble.observer.ConnectionObserver
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult

class SelectDeviceController(
    private val mContext: Context?,
    private val mViewMvc: SelectDeviceViewMvc,
    private val mListener: SelectDeviceViewMvc.Listener
) : SelectDeviceViewMvc.OnRefreshListener, ConnectionObserver {

    private var bleManager: BLEManager? = null
    private val scanner = BluetoothLeScannerCompat.getScanner()
    private val scanCallback = object : ScanCallback() {
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

    fun onStart() {
        registerListener(mListener)
        mViewMvc.registerOnRefreshListener(this)

        bleManager = BLEManager(mContext!!)
        bleManager!!.setConnectionObserver(this)
        startScan()
    }

    fun onStop() {
        unregisterListener(mListener)
        // TODO: handle
        // bluetoothManager.cancelDiscovery()
    }

    private fun startScan() {
        scanner.startScan(scanCallback)
    }

    override fun onRefreshClicked() {
        // TODO: handle
        // bluetoothManager.cancelDiscovery()
        // bluetoothManager.startDiscovery()
    }

    private fun registerListener(listener: SelectDeviceViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    private fun unregisterListener(listener: SelectDeviceViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {}

    override fun onDeviceConnected(device: BluetoothDevice) {}

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {}

    override fun onDeviceReady(device: BluetoothDevice) {
        Log.i(BLEManager.TAG, "onDeviceReady")
        bleManager!!.run()
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {}

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {}
}
