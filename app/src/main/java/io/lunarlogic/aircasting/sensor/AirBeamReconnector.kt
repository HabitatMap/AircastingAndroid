package io.lunarlogic.aircasting.sensor

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.events.SensorDisconnectedEvent
import io.lunarlogic.aircasting.exceptions.BLENotSupported
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import org.greenrobot.eventbus.EventBus

class AirBeamReconnector(
    private val mContext: Context,
    private val mAirBeamConnectorFactory: AirBeamConnectorFactory,
    private val mErrorHandler: ErrorHandler,
    private val mSessionsRepository: SessionsRepository,
    private val mBluetoothManager: BluetoothManager
): BroadcastReceiver(), AirBeamConnector.Listener {
    private var mSession: Session? = null
    private var mAirBeamConnector: AirBeamConnector? = null
    private var mCallback: (() -> Unit)? = null

    fun disconnect(session: Session) {
        sendDisconnectedEvent(session)
        updateSessionStatus(session, Session.Status.DISCONNECTED)
    }

    fun reconnect(session: Session, callback: () -> Unit) {
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
        }
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
        mAirBeamConnector?.reconnectMobileSession()
        updateSessionStatus(mSession, Session.Status.RECORDING)

        mCallback?.invoke()
    }

    override fun onConnectionFailed(deviceId: String) {
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
}
