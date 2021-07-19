package pl.llp.aircasting.sensor

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.bluetooth.BluetoothManager
import pl.llp.aircasting.events.AirBeamDiscoveryFailedEvent
import pl.llp.aircasting.events.AppToForegroundEvent
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.sensor.microphone.MicrophoneReader
import java.util.*
import javax.inject.Inject
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

    fun find(
        deviceSelector: (DeviceItem) -> Boolean,
        onDiscoverySuccessful: (deviceItem: DeviceItem) -> Unit,
        onDiscoveryFailed: () -> Unit,
        context: Context
    ) {

        mDeviceSelector = deviceSelector
        mOnDiscoverySuccessful = onDiscoverySuccessful
        mOnDiscoveryFailed = onDiscoveryFailed
        println("MARYSIA discovery service: deviceSelector ${mDeviceSelector}")
        val deviceItem = getDeviceItemFromPairedDevices(deviceSelector)

        println("MARYSIA discovery service: device item ${deviceItem}")

        if (deviceItem != null) {
            onDiscoverySuccessful(deviceItem)
        } else {
            println("MARYSIA dicovery service, should fail after timeout")
//            registerBluetoothDeviceFoundReceiver(context)
            mBluetoothManager.startDiscovery()
            failAfterTimeout()
        }
    }


    private fun failAfterTimeout() {
        println("MARYSIA: discovery service, fail after time out mDeviceItem ${mDeviceItem}")
        Timer().schedule(timerTask {
            if (mDeviceItem == null) {
                onDiscoveryFailed()
            }
        }, DISCOVERY_TIMEOUT)
    }

    private fun getDeviceItemFromPairedDevices(deviceSelector: (DeviceItem) -> Boolean): DeviceItem? {
        println("MARYSIA: trying to pair device mBluetoothManager ${mBluetoothManager}")
        println("MARYSIA: trying to pair device mBluetoothManager.pairedDeviceItems() ${mBluetoothManager.pairedDeviceItems()}")
        mBluetoothManager.pairedDeviceItems().forEach { item ->
            println("MARYSIA: paired item id ${item.id}")
        }
        return mBluetoothManager.pairedDeviceItems().find(deviceSelector)
    }

    fun registerBluetoothDeviceFoundReceiver(context: Context) {
        println("MARYSIA: registering Bluetooth devive found")
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(this, filter)
    }

    fun unRegisterBluetoothDeviceFoundReceiver(context: Context) {
        context.unregisterReceiver(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        // TODO: MAYBE if we register for the receiver while we are in the foreground? HOW?
        println("MARYSIA: ----- onReceive")
        when(intent.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                println("MARYSIA: BluetoothDevice.ACTION_FOUND")
                device?.let { onBluetoothDeviceFound(context, DeviceItem(device)) }
            }
            else -> {
                println("MARYSIA: Broadcast onReceive action ${intent.action}")
            }
        }
    }

    private fun onBluetoothDeviceFound(context: Context, deviceItem: DeviceItem) {
        if (mDeviceSelector?.invoke(deviceItem) == true) {
            mDeviceItem = deviceItem
            unRegisterBluetoothDeviceFoundReceiver(context)

            mOnDiscoverySuccessful?.invoke(deviceItem)
        }
    }

    private fun onDiscoveryFailed() {
        EventBus.getDefault().post(AirBeamDiscoveryFailedEvent())
    }
}
