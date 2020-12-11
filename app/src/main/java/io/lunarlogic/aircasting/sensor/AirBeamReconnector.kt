package io.lunarlogic.aircasting.sensor

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.events.DisconnectExternalSensorsEvent
import io.lunarlogic.aircasting.exceptions.BLENotSupported
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import org.greenrobot.eventbus.EventBus

class AirBeamReconnector(
    private val mContext: Context,
    private val mAirBeamConnectorFactory: AirBeamConnectorFactory,
    private val mErrorHandler: ErrorHandler
): BroadcastReceiver(), AirBeamConnector.Listener {
    private var mDeviceIdToReconnect: String? = null
    private var mAirBeamConnector: AirBeamConnector? = null

    fun disconnect() {
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
    }

    fun reconnect(deviceId: String) {
        // disconnecting first to make sure the connector thread is stopped correctly etc
        disconnect()

        mDeviceIdToReconnect = deviceId

        val bm = BluetoothManager()
        // TODO: lookup in pairedDevices (for AB1 and AB2) if in paired devices that awesome
        // but otherwise start discovery again
        registerBluetoothDeviceFoundReceiver()
        bm.startDiscovery()
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
        }
    }

    private fun onBluetoothDeviceFound(deviceItem: DeviceItem) {
        if (deviceItem.id == mDeviceIdToReconnect) {
            unRegisterBluetoothDeviceFoundReceiver()
            reconnect(deviceItem)
        }
    }

    private fun reconnect(deviceItem: DeviceItem) {
        mAirBeamConnector = mAirBeamConnectorFactory.get(deviceItem)
        mAirBeamConnector?.registerListener(this)
        try {
            mAirBeamConnector?.connect(deviceItem)
        } catch (e: BLENotSupported) {
            mErrorHandler.handleAndDisplay(e)
        }
    }

    override fun onConnectionSuccessful(deviceId: String) {
        mAirBeamConnector?.reconnectMobileSession()
    }
}
