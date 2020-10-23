package io.lunarlogic.aircasting.screens.new_session.select_device

import android.content.Context
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult

class SelectDeviceController(
    private val mContext: Context?,
    private val mViewMvc: SelectDeviceViewMvc,
    private val mListener: SelectDeviceViewMvc.Listener
) : SelectDeviceViewMvc.OnRefreshListener {

    private val scanner = BluetoothLeScannerCompat.getScanner()
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            mViewMvc.addDeviceItem(DeviceItem(device))
        }
    }

    fun onStart() {
        registerListener(mListener)
        mViewMvc.registerOnRefreshListener(this)
        mViewMvc.bindDeviceItems(emptyList())

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
}
