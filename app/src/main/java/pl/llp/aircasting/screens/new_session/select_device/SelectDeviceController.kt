package pl.llp.aircasting.screens.new_session.select_device

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import pl.llp.aircasting.bluetooth.BluetoothManager
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.exceptions.SensorDisconnectedError

class SelectDeviceController(
    private var mContext: Context?,
    private var mViewMvc: SelectDeviceViewMvc?,
    private val bluetoothManager: BluetoothManager,
    private val mListener: SelectDeviceViewMvc.Listener
) : BroadcastReceiver(), SelectDeviceViewMvc.OnRefreshListener {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                device?.let { mViewMvc?.addDeviceItem(DeviceItem(it)) }
            }
        }
    }

    fun onStart() {
        bindDevices()
        registerListener(mListener)
        registerBluetoothDeviceFoundReceiver()
        mViewMvc?.registerOnRefreshListener(this)

        startScan()
    }

    fun onStop() {
        unregisterListener(mListener)
        unRegisterBluetoothDeviceFoundReceiver()
        stopScan()
    }

    fun onDestroy() {
        mContext = null
        mViewMvc = null
    }

    private fun startScan() {
        bluetoothManager.startDiscovery()
    }

    private fun stopScan() {
        bluetoothManager.cancelDiscovery()
    }

    override fun onRefreshClicked() {
        stopScan()
        startScan()
    }

    private fun bindDevices() {
        val deviceItems = bluetoothManager.pairedDeviceItems()
        mViewMvc?.bindDeviceItems(deviceItems)
    }

    private fun registerListener(listener: SelectDeviceViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    private fun unregisterListener(listener: SelectDeviceViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }

    private fun registerBluetoothDeviceFoundReceiver() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        mContext?.registerReceiver(this, filter)
    }

    private fun unRegisterBluetoothDeviceFoundReceiver() {
        mContext?.unregisterReceiver(this)
    }
}
