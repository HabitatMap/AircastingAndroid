package pl.llp.aircasting.sensor

import android.content.Context
import kotlinx.coroutines.CoroutineScope
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
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.events.AirBeamDiscoveryFailedEvent
import java.util.*
import kotlin.concurrent.timerTask

class AirBeamReconnector(
    private val mContext: Context,
    private val mSessionsRepository: SessionsRepository,
    private val mAirBeamDiscoveryService: AirBeamDiscoveryService
) {
    interface Listener {
        fun beforeReconnection(session: Session)
        fun errorCallback()
        fun finallyCallback(session: Session)
    }

    private var mSession: Session? = null
    private var mErrorCallback: (() -> Unit)? = null
    private var mFinallyCallback: (() -> Unit)? = null
    private var mListener: AirBeamReconnector.Listener? = null

    var mReconnectionTriesNumber: Int? = null
    private val RECONNECTION_TRIES_MAX = 15
    private val RECONNECTION_TRIES_INTERVAL = 15000L // 15s between reconnection tries

    private val RECONNECTION_TRIES_RESET_DELAY = RECONNECTION_TRIES_INTERVAL + 5000L // we need to have delay
    // greater than interval between tries so we don't trigger another try after we successfully reconnected

    fun registerListener(listener: AirBeamReconnector.Listener) {
        mListener = listener
    }

    fun disconnect(session: Session) {
        println("MARYSIA airbeam reconnector disconnect sending event")
        sendDisconnectedEvent(session)
        updateSessionStatus(session, Session.Status.DISCONNECTED)
    }

    fun reconnect(session: Session, errorCallback: () -> Unit, finallyCallback: () -> Unit) {
        println("MARYSIA reconnect, registering to Eventbus ")
        EventBus.getDefault().safeRegister(this)

        if (mReconnectionTriesNumber != null) {
            println("MARYSIA:  reconnection try # ${mReconnectionTriesNumber}")
            mReconnectionTriesNumber?.let { tries ->
                if (tries > RECONNECTION_TRIES_MAX) {
                    return
                }
            }
        } else {
            // disconnecting first to make sure the connector thread is stopped correctly etc
            println("MARYSIA: AirBeamReConnector reconnect else sending event")
            sendDisconnectedEvent(session)
        }

        mSession = session
        mErrorCallback = errorCallback
        mFinallyCallback = finallyCallback

        println("MARYSIA: reconnect try, mSession  device id ${session.deviceId}")
        reconnect(session.deviceId)
    }

    fun initReconnectionTries(session: Session) {
        println("MARYSIA: init reconnection")
        if (mReconnectionTriesNumber != null) return
        mListener?.beforeReconnection(session)
        mReconnectionTriesNumber = 1
        reconnect(session, { mListener?.errorCallback() }, { mListener?.finallyCallback(session) })
    }

    private fun reconnect(deviceId: String?) {
        println("MARYSIA: is this reconnect(deviceItem) callback is called?")
        AirBeamReconnectSessionService.startService(mContext, deviceId, mSession?.uuid)
    }

    private fun onDiscoveryFailed() {
        println("MARYSIA:  reconnection onDiscoveryFailed - before if")
        if (mReconnectionTriesNumber != null && mReconnectionTriesNumber!! < RECONNECTION_TRIES_MAX) {
            println("MARYSIA:  reconnection onDiscoveryFailed")
            mReconnectionTriesNumber = mReconnectionTriesNumber?.plus(1)
            Thread.sleep(RECONNECTION_TRIES_INTERVAL)
            if (mSession != null && mErrorCallback != null && mFinallyCallback != null) {
                reconnect(mSession!!, mErrorCallback!!, mFinallyCallback!!)
            }
        } else {
            mFinallyCallback?.invoke()
        }
    }

    private fun sendDisconnectedEvent(session: Session) {
        val deviceId = session.deviceId
        deviceId?.let { EventBus.getDefault().post(SensorDisconnectedEvent(deviceId, session.uuid)) }
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
        println("MARYSIA: reconnection successful")
        resetTriesNumberWithDelay()

        updateSessionStatus(mSession, Session.Status.RECORDING)

        mFinallyCallback?.invoke()
        unregisterFromEventBus()
    }

    private fun resetTriesNumberWithDelay() {
        if (mReconnectionTriesNumber != null) {
            val timerTask = timerTask {
                mReconnectionTriesNumber = null
                println("MARYSIA: nulling mReconnectionTriesNumber ${mReconnectionTriesNumber}")
            }
            Timer().schedule(timerTask, RECONNECTION_TRIES_RESET_DELAY)
        }
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamConnectionFailedEvent) {
        println("MARYSIA:  reconnection AirBeamConnectionFailedEvent - before if")
        if (mReconnectionTriesNumber != null) {
            println("MARYSIA:  reconnection AirBeamConnectionFailedEvent")
            mReconnectionTriesNumber?.let { tries ->
                if (tries > RECONNECTION_TRIES_MAX) {
                    // TODO: should we invoke callbacks here?
                    return
                } else {
                    mReconnectionTriesNumber = mReconnectionTriesNumber?.plus(1)
                    Thread.sleep(RECONNECTION_TRIES_INTERVAL)
                    reconnect(event.deviceItem.id)
                }
            }
        } else {
            mErrorCallback?.invoke()
            mFinallyCallback?.invoke()
            unregisterFromEventBus()
        }
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamDiscoveryFailedEvent) {
        onDiscoveryFailed()
    }

    private fun unregisterFromEventBus() {
        EventBus.getDefault().unregister(this);
    }
}
