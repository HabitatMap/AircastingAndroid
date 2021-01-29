package io.lunarlogic.aircasting.sensor

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.events.AirBeamConnectionFailedEvent
import io.lunarlogic.aircasting.events.AirBeamConnectionSuccessfulEvent
import io.lunarlogic.aircasting.events.SensorDisconnectedEvent
import io.lunarlogic.aircasting.lib.safeRegister
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import kotlin.concurrent.timerTask

class AirBeamReconnector(
    private val mContext: Context,
    private val mSessionsRepository: SessionsRepository,
    private val mBluetoothManager: BluetoothManager
): BroadcastReceiver() {
    private var mSession: Session? = null
    private var mDeviceItem: DeviceItem? = null
    private var mAirBeamConnector: AirBeamConnector? = null
    private var mCallback: (() -> Unit)? = null
    private val DISCOVERY_TIMEOUT = 5000L

    fun disconnect(session: Session) {
        sendDisconnectedEvent(session)
        updateSessionStatus(session, Session.Status.DISCONNECTED)
    }

    fun reconnect(session: Session, callback: () -> Unit) {
        EventBus.getDefault().safeRegister(this)

        // disconnecting first to make sure the connector thread is stopped correctly etc
        sendDisconnectedEvent(session)

        mSession = session
        mCallback = callback

        val deviceItem = getDeviceItemFromPairedDevices(session)

        if (deviceItem != null) {
            reconnect(deviceItem)
        } else {
            registerBluetoothDeviceFoundReceiver()
            mBluetoothManager.startDiscovery()
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

    private fun getDeviceItemFromPairedDevices(session: Session): DeviceItem? {
        return mBluetoothManager.pairedDeviceItems().find { deviceItem -> deviceItem.id == session.deviceId }
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
        if (deviceItem.id == mSession?.deviceId) {
            mDeviceItem = deviceItem
            unRegisterBluetoothDeviceFoundReceiver()
            reconnect(deviceItem)
        }
    }

    private fun reconnect(deviceItem: DeviceItem) {
        AirBeamService.startService(mContext, deviceItem, mSession?.uuid)
    }

    fun onDiscoveryFailed() {
        mCallback?.invoke()
    }

    private fun sendDisconnectedEvent(session: Session) {
        val deviceId = session.deviceId
        deviceId?.let { EventBus.getDefault().post(SensorDisconnectedEvent(deviceId)) }
    }

    private fun updateSessionStatus(session: Session?, status: Session.Status) {
        session?.let { session ->
            DatabaseProvider.runQuery {
                mSessionsRepository.updateSessionStatus(session, status)
            }
        }
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamConnectionSuccessfulEvent) {
        mAirBeamConnector?.reconnectMobileSession()
        updateSessionStatus(mSession, Session.Status.RECORDING)

        mCallback?.invoke()
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamConnectionFailedEvent) {
        mCallback?.invoke()
    }
}
