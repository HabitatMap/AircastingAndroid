package pl.llp.aircasting.sensor

import android.content.Context
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.events.AirBeamConnectionFailedEvent
import pl.llp.aircasting.events.AirBeamConnectionSuccessfulEvent
import pl.llp.aircasting.events.SensorDisconnectedEvent
import pl.llp.aircasting.lib.safeRegister
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class AirBeamReconnector(
    private val mContext: Context,
    private val mSessionsRepository: SessionsRepository,
    private val mAirBeamDiscoveryService: AirBeamDiscoveryService
) {
    private var mSession: Session? = null
    private var mErrorCallback: (() -> Unit)? = null
    private var mFinallyCallback: (() -> Unit)? = null

    fun disconnect(session: Session) {
        sendDisconnectedEvent(session)
        updateSessionStatus(session, Session.Status.DISCONNECTED)
    }

    fun reconnect(session: Session, errorCallback: () -> Unit, finallyCallback: () -> Unit) {
        EventBus.getDefault().safeRegister(this)

        // disconnecting first to make sure the connector thread is stopped correctly etc
        sendDisconnectedEvent(session)

        mSession = session
        mErrorCallback = errorCallback
        mFinallyCallback = finallyCallback

        mAirBeamDiscoveryService.find(
            deviceSelector = { deviceItem -> deviceItem.id == session.deviceId },
            onDiscoverySuccessful = { deviceItem -> reconnect(deviceItem) },
            onDiscoveryFailed = { onDiscoveryFailed() }
        )
    }

    private fun reconnect(deviceItem: DeviceItem) {
        AirBeamReconnectSessionService.startService(mContext, deviceItem, mSession?.uuid)
    }

    private fun onDiscoveryFailed() {
        mFinallyCallback?.invoke()
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
        updateSessionStatus(mSession, Session.Status.RECORDING)

        mFinallyCallback?.invoke()
        unregisterFromEventBus()
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamConnectionFailedEvent) {
        mErrorCallback?.invoke()
        mFinallyCallback?.invoke()
        unregisterFromEventBus()
    }

    private fun unregisterFromEventBus() {
        EventBus.getDefault().unregister(this);
    }
}
