package io.lunarlogic.aircasting.screens.new_session.select_device

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.lunarlogic.aircasting.bluetooth.BluetoothManager

class SelectDeviceController(
    private val mContext: Context?,
    private val mViewMvc: SelectDeviceViewMvc,
    private val bluetoothManager: BluetoothManager,
    private val mListener: SelectDeviceViewMvc.Listener
) : BroadcastReceiver() {

    fun onStart() {
        bindDevices()
        registerListener(mListener)
        registerBluetoothDeviceFoundReceiver()
        bluetoothManager.startDiscovery()
    }

    fun onStop() {
        unregisterListener(mListener)
        unRegisterBluetoothDeviceFoundReceiver()
        bluetoothManager.cancelDiscovery()
    }

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                device?.let { mViewMvc.addDeviceItem(DeviceItem(it)) }
            }
        }
    }

    private fun bindDevices() {
        val deviceItems = bluetoothManager.pairedDeviceItems()
        mViewMvc.bindDeviceItems(deviceItems)
    }

    private fun registerListener(listener: SelectDeviceViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    private fun unregisterListener(listener: SelectDeviceViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }

    private fun registerBluetoothDeviceFoundReceiver() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        mContext?.registerReceiver(this, filter)
    }

    private fun unRegisterBluetoothDeviceFoundReceiver() {
        mContext?.unregisterReceiver(this)
    }
}
