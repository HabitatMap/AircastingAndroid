package io.lunarlogic.aircasting.sensor

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.exceptions.BLENotSupported
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.sensor.airbeam3.AirBeam3Configurator
import org.greenrobot.eventbus.EventBus
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.timerTask

class AirBeamSyncService: SensorService(),
    AirBeamConnector.Listener {

    @Inject
    lateinit var airbeamConnectorFactory: AirBeamConnectorFactory

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    @Inject
    lateinit var errorHandler: ErrorHandler

    private var mDeviceItem: DeviceItem? = null
    private var mAirBeamConnector: AirBeamConnector? = null
    private val DISCOVERY_TIMEOUT = 5000L

    private var clearSDCard = false

    private val mReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    device?.let { onBluetoothDeviceFound(DeviceItem(device)) }
                }
            }
        }
    }

    companion object {
        val CLEAR_SD_CARD_KEY = "inputExtraClearSDCard"

        fun startService(context: Context, clearSDCard: Boolean = false) {
            val startIntent = Intent(context, AirBeamSyncService::class.java)

            startIntent.putExtra(CLEAR_SD_CARD_KEY, clearSDCard)

            ContextCompat.startForegroundService(context, startIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as AircastingApplication
        val appComponent = app.appComponent
        appComponent.inject(this)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun startSensor(intent: Intent?) {
        intent ?: return

        this.clearSDCard = intent.getBooleanExtra(CLEAR_SD_CARD_KEY, false)

        val deviceItem = getDeviceItemFromPairedDevices()

        if (deviceItem != null) {
            connect(deviceItem)
        } else {
            registerBluetoothDeviceFoundReceiver()
            bluetoothManager.startDiscovery()
            failAfterTimeout()
        }
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
        applicationContext.registerReceiver(mReceiver, filter)
    }

    private fun unRegisterBluetoothDeviceFoundReceiver() {
        applicationContext.unregisterReceiver(mReceiver)
    }

    private fun onBluetoothDeviceFound(deviceItem: DeviceItem) {
        if (deviceItem.isSyncable()) {
            mDeviceItem = deviceItem
            unRegisterBluetoothDeviceFoundReceiver()
            connect(deviceItem)
        }
    }

    private fun connect(deviceItem: DeviceItem) {
        mAirBeamConnector = airbeamConnectorFactory.get(deviceItem)

        mAirBeamConnector?.registerListener(this)
        try {
            mAirBeamConnector?.connect(deviceItem)
        } catch (e: BLENotSupported) {
            errorHandler.handleAndDisplay(e)
            onConnectionFailed(deviceItem.id)
        }
    }

    override fun onStopService() {
        // nothing
    }

    override fun notificationMessage(): String {
        return getString(R.string.ab_sync_service_notification_message)
    }

    override fun onConnectionSuccessful(deviceItem: DeviceItem, sessionUUID: String?) {
        showInfo("Connection to ${deviceItem.name} successful.")

        if (clearSDCard) {
            mAirBeamConnector?.clearSDCard()
        } else {
            mAirBeamConnector?.sync()
        }
    }

    override fun onConnectionFailed(deviceId: String) {
        // TODO: temporary thing
        showInfo("Connection to ${mDeviceItem?.name} failed.")
    }

    override fun onDisconnect(deviceId: String) {
        stopSelf()
    }

    fun onDiscoveryFailed() {
        // TODO: temporary thing
        showInfo("Discovery failed.")
    }

    private fun showInfo(info: String) {
        EventBus.getDefault().post(AirBeam3Configurator.SyncEvent(info))
    }

    private fun getDeviceItemFromPairedDevices(): DeviceItem? {
        return bluetoothManager.pairedDeviceItems().find { deviceItem -> deviceItem.isSyncable() }
    }
}
