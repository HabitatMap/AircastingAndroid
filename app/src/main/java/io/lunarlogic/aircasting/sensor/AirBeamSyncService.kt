package io.lunarlogic.aircasting.sensor

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.exceptions.BLENotSupported
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.sensor.airbeam3.AirBeam3Configurator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.concurrent.timerTask

class AirBeamSyncService(
    private val mContext: Context,
    private val mAirBeamConnectorFactory: AirBeamConnectorFactory,
    private val mErrorHandler: ErrorHandler,
    private val mBluetoothManager: BluetoothManager
) : BroadcastReceiver(), AirBeamConnector.Listener {

    private var mDeviceItem: DeviceItem? = null
    private var mAirBeamConnector: AirBeamConnector? = null
    private val DISCOVERY_TIMEOUT = 5000L

    private var clearSDCard = false

    fun run(clearSDCard: Boolean = false) {
        this.clearSDCard = clearSDCard

        // disconnect?
        registerBluetoothDeviceFoundReceiver()
        mBluetoothManager.startDiscovery()
        failAfterTimeout()
    }

    private fun failAfterTimeout() {
        Timer().schedule(timerTask {
            if (mDeviceItem == null) {
                onDiscoveryFailed()
            }
        }, DISCOVERY_TIMEOUT)
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
        // TODO: any more checking?
        if (deviceItem.isSyncable()) {
            mDeviceItem = deviceItem
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

    override fun onConnectionSuccessful(deviceItem: DeviceItem) {
        showInfo("Connection to ${deviceItem.displayName()} successful.")

        if (clearSDCard) {
            mAirBeamConnector?.clearSDCard()
        } else {
            mAirBeamConnector?.sync()
        }
    }

    override fun onConnectionFailed(deviceId: String) {
        // TODO: temporary thing
        showInfo("Connection to ${mDeviceItem?.displayName()} failed.")
    }

    fun onDiscoveryFailed() {
        // TODO: temporary thing
        showInfo("Discovery failed.")
    }

    private fun showInfo(info: String) {
        EventBus.getDefault().post(AirBeam3Configurator.SyncEvent(info))
    }
}
