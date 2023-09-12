package pl.llp.aircasting.util.helpers.sensor.services

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.events.AirBeamDiscoveryFailedEvent
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import java.util.*
import kotlin.concurrent.timerTask

open class AirBeamDiscoveryService(
    private val mContext: Context,
    private val mBluetoothManager: BluetoothManager
): BroadcastReceiver() {

    private var mDeviceItem: DeviceItem? = null
    private var mDeviceSelector: ((DeviceItem) -> Boolean)? = null
    private var mOnDiscoverySuccessful: ((deviceItem: DeviceItem) -> Unit)? = null
    private var mOnDiscoveryFailed: (() -> Unit)? = null

    private val DISCOVERY_TIMEOUT = 5000L
    private val TAG = "AirBeamDiscoveryService"

    fun find(
        deviceSelector: (DeviceItem) -> Boolean,
        onDiscoverySuccessful: (deviceItem: DeviceItem) -> Unit,
        onDiscoveryFailed: () -> Unit
    ) {
        mDeviceSelector = deviceSelector
        mOnDiscoverySuccessful = onDiscoverySuccessful
        mOnDiscoveryFailed = onDiscoveryFailed
        val deviceItem = getDeviceItemFromPairedDevices(deviceSelector)

        if (deviceItem != null) {
            onDiscoverySuccessful(deviceItem)
        } else {
            registerBluetoothDeviceFoundReceiver()
            mBluetoothManager.startDiscovery()
            failAfterTimeout()
        }
    }

    fun reset() {
        Log.d(TAG, "Resetting discovery service")
        mDeviceSelector = null
        mOnDiscoverySuccessful = null
        mOnDiscoveryFailed = null
    }

    private fun failAfterTimeout() {
        Timer().schedule(timerTask {
            if (mDeviceItem == null) {
                onDiscoveryFailed()
            }
        }, DISCOVERY_TIMEOUT)
    }

    private fun getDeviceItemFromPairedDevices(deviceSelector: (DeviceItem) -> Boolean): DeviceItem? {
        return mBluetoothManager.pairedDeviceItems().find(deviceSelector)
    }

    private fun registerBluetoothDeviceFoundReceiver() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        mContext.registerReceiver(this, filter)
    }

    private fun unRegisterBluetoothDeviceFoundReceiver() {
        mContext.unregisterReceiver(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                device?.let { onBluetoothDeviceFound(DeviceItem(device)) }
            }
            else -> {
                Log.d(TAG, "Broadcast onReceive action ${intent.action}")
            }
        }
    }

    private fun onBluetoothDeviceFound(deviceItem: DeviceItem) {
        if (mDeviceSelector?.invoke(deviceItem) == true) {
            mDeviceItem = deviceItem
            unRegisterBluetoothDeviceFoundReceiver()
            Log.d(TAG, "Device found: $deviceItem")
            mOnDiscoverySuccessful?.invoke(deviceItem)
        }
    }

    private fun onDiscoveryFailed() {
        EventBus.getDefault().post(AirBeamDiscoveryFailedEvent())
    }
}
