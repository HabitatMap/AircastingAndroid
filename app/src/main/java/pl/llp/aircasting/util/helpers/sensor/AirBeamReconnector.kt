package pl.llp.aircasting.util.helpers.sensor

import android.content.Context
import android.util.Log
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.events.*
import pl.llp.aircasting.util.extensions.safeRegister
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.schedule
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

    private lateinit var mSession: Session
    private lateinit var mErrorCallback: (() -> Unit)
    private lateinit var mFinallyCallback: (() -> Unit)
    private var mListener: Listener? = null

    private var mStandaloneMode = AtomicBoolean(false)
    var mReconnectionTriesNumber: Int? = null
    private val RECONNECTION_TRIES_MAX = 15
    private val RECONNECTION_TRIES_INTERVAL = 15000L // 15s between reconnection tries

    private val RECONNECTION_TRIES_RESET_DELAY =
        RECONNECTION_TRIES_INTERVAL + 5000L // we need to have delay
    // greater than interval between tries so we don't trigger another try after we successfully reconnected

    fun registerListener(listener: Listener) {
        mListener = listener
    }

    fun disconnect(session: Session) {
        mStandaloneMode.set(true)
        sendDisconnectedEvent(session)
        updateSessionStatus(session, Session.Status.DISCONNECTED)
    }

    fun reconnect(
        session: Session,
        deviceItem: DeviceItem?,
        errorCallback: () -> Unit,
        finallyCallback: () -> Unit
    ) {
        EventBus.getDefault().safeRegister(this)

        if (mReconnectionTriesNumber != null) {
            mReconnectionTriesNumber?.let { tries ->
                if (tries > RECONNECTION_TRIES_MAX) {
                    return
                }
            }
        } else {
            // disconnecting first to make sure the connector thread is stopped correctly etc
            sendDisconnectedEvent(session)
        }

        mSession = session
        mErrorCallback = errorCallback
        mFinallyCallback = finallyCallback

        if (deviceItem?.type == DeviceItem.Type.AIRBEAM3) {
            reconnect(session.deviceId, deviceItem)
        } else {
            mAirBeamDiscoveryService.find(
                deviceSelector = { mDeviceItem -> mDeviceItem.id == session.deviceId },
                onDiscoverySuccessful = { mDeviceItem ->
                    startReconnectServiceWhenDeviceIsReady(session.deviceId, mDeviceItem)
                },
                onDiscoveryFailed = { onDiscoveryFailed() }
            )
        }
    }

    fun initReconnectionTries(session: Session, deviceItem: DeviceItem?) {
        if (mStandaloneMode.get()) return
        if (mReconnectionTriesNumber != null) return
        mListener?.beforeReconnection(session)
        mReconnectionTriesNumber = 1
        reconnect(
            session,
            deviceItem,
            { mListener?.errorCallback() },
            { mListener?.finallyCallback(session) })
    }

    private fun reconnect(deviceId: String?, deviceItem: DeviceItem) {
        try {
            AirBeamReconnectSessionService.startService(
                mContext,
                deviceId,
                deviceItem,
                mSession.uuid
            )
        } catch (e: Exception) {
            Log.d("start Service error: ", e.message.toString())
        }
    }

    private fun startReconnectServiceWhenDeviceIsReady(deviceId: String?, deviceItem: DeviceItem) {
        // sometimes AirBeam 2 takes 28 seconds to be ready and sometimes less(16s),
        // so we'll wait for (at least) 16 seconds for the device to be fully ready
        Timer().schedule(28000) {
            reconnect(deviceId, deviceItem)
        }
    }

    private fun onDiscoveryFailed() {
        if (mReconnectionTriesNumber != null && mReconnectionTriesNumber!! < RECONNECTION_TRIES_MAX) {
            mReconnectionTriesNumber = mReconnectionTriesNumber?.plus(1)
            Thread.sleep(RECONNECTION_TRIES_INTERVAL)

            reconnect(mSession, null, mErrorCallback, mFinallyCallback)

        } else mFinallyCallback.invoke()
    }

    private fun sendDisconnectedEvent(session: Session) {
        val deviceId = session.deviceId
        deviceId?.let {
            EventBus.getDefault().post(SensorDisconnectedEvent(deviceId, null, session.uuid))
        }
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
        resetTriesNumberWithDelay()
        mAirBeamDiscoveryService.reset()

        updateSessionStatus(mSession, Session.Status.RECORDING)

        mFinallyCallback.invoke()
        unregisterFromEventBus()
    }

    private fun resetTriesNumberWithDelay() {
        if (mReconnectionTriesNumber != null) {
            val timerTask = timerTask {
                mReconnectionTriesNumber = null
            }
            Timer().schedule(timerTask, RECONNECTION_TRIES_RESET_DELAY)
        }
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamConnectionFailedEvent) {
        if (mReconnectionTriesNumber != null) {
            mReconnectionTriesNumber?.let { tries ->
                if (tries > RECONNECTION_TRIES_MAX) {
                    finalizeReconnectionWithError()
                    resetTriesNumberWithDelay()
                    return
                } else {
                    mReconnectionTriesNumber = mReconnectionTriesNumber?.plus(1)
                    Thread.sleep(RECONNECTION_TRIES_INTERVAL)
                    reconnect(event.deviceItem.id, event.deviceItem)
                }
            }
        } else {
            finalizeReconnectionWithError()
        }
    }

    private fun finalizeReconnectionWithError() {
        mErrorCallback.invoke()
        finalizeReconnection()
    }

    @Subscribe
    fun onMessageEvent(event: StopRecordingEvent) {
        finalizeReconnection()
    }

    private fun finalizeReconnection() {
        mAirBeamDiscoveryService.reset()
        mFinallyCallback.invoke()
        unregisterFromEventBus()
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamDiscoveryFailedEvent) {
        onDiscoveryFailed()
    }

    private fun unregisterFromEventBus() {
        EventBus.getDefault().unregister(this)
    }
}
