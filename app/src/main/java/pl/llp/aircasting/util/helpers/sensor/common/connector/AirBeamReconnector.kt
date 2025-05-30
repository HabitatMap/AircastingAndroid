package pl.llp.aircasting.util.helpers.sensor.common.connector

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.util.LogKeys.bluetoothReconnection
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.AirbeamConnectionStatus
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.modules.SyncActiveFlow
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.events.AirBeamConnectionFailedEvent
import pl.llp.aircasting.util.events.AirBeamDiscoveryFailedEvent
import pl.llp.aircasting.util.events.SensorDisconnectedEvent
import pl.llp.aircasting.util.events.StandaloneModeEvent
import pl.llp.aircasting.util.events.StopRecordingEvent
import pl.llp.aircasting.util.extensions.eventbus
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.helpers.sensor.services.AirBeamDiscoveryService
import pl.llp.aircasting.util.helpers.sensor.services.AirBeamReconnectSessionService
import java.util.concurrent.ConcurrentHashMap

class AirBeamReconnector(
    private val mContext: Context,
    private val mSessionsRepository: SessionsRepository,
    private val mAirBeamDiscoveryService: AirBeamDiscoveryService,
    private val coroutineScope: CoroutineScope,
    private val sessionUuidByStandaloneMode: MutableMap<String, Boolean> = ConcurrentHashMap(),
    private val connectionStatusFlow: StateFlow<AirbeamConnectionStatus?>,
    @SyncActiveFlow
    private val syncStatusFlow: SharedFlow<Boolean>,
) {
    private var mSession: Session? = null
    private var mErrorCallback: (() -> Unit)? = null
    private var mFinallyCallback: (() -> Unit)? = null

    var mReconnectionTriesNumber: Int? = null
    private val RECONNECTION_TRIES_MAX = 50
    private val RECONNECTION_TRIES_INTERVAL = 2000L // 2s between reconnection tries

    fun disconnect(session: Session) {
        sessionUuidByStandaloneMode[session.uuid] = true
        sendDisconnectedEvent(session)
        updateSessionStatus(session, Session.Status.DISCONNECTED)
        eventbus.post(StandaloneModeEvent(session.uuid))
    }

    fun reconnect(
        session: Session,
        deviceItem: DeviceItem?,
        errorCallback: (() -> Unit)? = null,
        finallyCallback: (() -> Unit)? = null,
    ) {
        eventbus.safeRegister(this)
        observeConnectionStatus()
        observeSyncStatus()

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
            reconnect(deviceItem)
        } else {
            mAirBeamDiscoveryService.find(
                deviceSelector = { device -> device.id == session.deviceId },
                onDiscoverySuccessful = { device -> reconnect(device) },
                onDiscoveryFailed = { onDiscoveryFailed() }
            )
        }
    }

    fun tryToReconnectPeriodically(session: Session, deviceItem: DeviceItem?) {
        if (sessionUuidByStandaloneMode[session.uuid] == true) {
            Log.e(TAG, "Will not reconnect: Session is in standalone mode")
            return
        }
        if (mReconnectionTriesNumber != null) {
            Log.e(TAG, "Will not reconnect: Reconnection is already in progress")
            return
        }

        mReconnectionTriesNumber = 1
        reconnect(session, deviceItem)
    }

    private fun reconnect(deviceItem: DeviceItem? = null) {
        try {
            Log.d(TAG, "Starting AirBeamReconnectSessionService")
            AirBeamReconnectSessionService.startService(
                mContext,
                deviceItem,
                mSession?.uuid
            )
            eventbus.postSticky(ReconnectionEvent(mSession?.uuid, true))
        } catch (e: Exception) {
            Log.e(
                bluetoothReconnection, "Attempt to start reconnection service failed\n" +
                        e.stackTraceToString()
            )
        }
    }

    private fun onDiscoveryFailed() {
        if (mReconnectionTriesNumber != null && mReconnectionTriesNumber!! < RECONNECTION_TRIES_MAX) {
            mReconnectionTriesNumber = mReconnectionTriesNumber?.plus(1)
            Thread.sleep(RECONNECTION_TRIES_INTERVAL)

            val session = mSession ?: return
            reconnect(session, null, mErrorCallback, mFinallyCallback)
        } else {
            finalizeReconnectionWithError()
        }
    }

    private fun sendDisconnectedEvent(session: Session) {
        val deviceId = session.deviceId
        deviceId?.let {
            Log.d(TAG, "Posting SensorDisconnectedEvent")
            eventbus.post(SensorDisconnectedEvent(deviceId, null, session.uuid))
        }
    }

    private fun updateSessionStatus(session: Session?, status: Session.Status) {
        Log.v(TAG, "Updating session status")
        session?.let { session ->
            coroutineScope.launch {
                mSessionsRepository.updateSessionStatus(session, status)
            }
        }
    }
    private fun observeConnectionStatus() = coroutineScope.launch {
        connectionStatusFlow.filterNotNull().collect {
            val correctSesssionConnected = it.isConnected && it.sessionUUID == mSession?.uuid
            if (correctSesssionConnected) onConnectedSuccessful()
            else if (it.isConnected) finalizeReconnection()
        }
    }
    private fun onConnectedSuccessful() {
        updateSessionStatus(mSession, Session.Status.RECORDING)
        finalizeReconnection()
    }

    private fun observeSyncStatus() = coroutineScope.launch {
        syncStatusFlow.collect { isSyncActive ->
            if (isSyncActive) finalizeReconnection()
        }
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamConnectionFailedEvent) {
        if (mReconnectionTriesNumber != null) {
            mReconnectionTriesNumber?.let { tries ->
                if (tries > RECONNECTION_TRIES_MAX) {
                    finalizeReconnectionWithError()
                    return
                } else {
                    mReconnectionTriesNumber = mReconnectionTriesNumber?.plus(1)
                    Thread.sleep(RECONNECTION_TRIES_INTERVAL)
                    reconnect(event.deviceItem)
                }
            }
        } else {
            finalizeReconnectionWithError()
        }
    }

    private fun finalizeReconnectionWithError() {
        Log.e(
            bluetoothReconnection,
            "Finalized with error. Reconnection tries: $mReconnectionTriesNumber"
        )
        mErrorCallback?.invoke()
        finalizeReconnection()
    }

    @Subscribe
    fun onMessageEvent(event: StopRecordingEvent) {
        finalizeReconnection()
    }

    private fun finalizeReconnection() {
        Log.d(TAG, "Finalizing reconnection")
        mAirBeamDiscoveryService.reset()
        mReconnectionTriesNumber = null
        mFinallyCallback?.invoke()
        eventbus.postSticky(ReconnectionEvent(mSession?.uuid, false))
        unregisterFromEventBus()
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamDiscoveryFailedEvent) {
        onDiscoveryFailed()
    }

    private fun unregisterFromEventBus() {
        eventbus.unregister(this)
    }

    class ReconnectionEvent(val sessionUuid: String?, val inProgress: Boolean = false)
}