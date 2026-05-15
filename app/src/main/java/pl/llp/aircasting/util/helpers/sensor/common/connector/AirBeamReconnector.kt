package pl.llp.aircasting.util.helpers.sensor.common.connector

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import pl.llp.aircasting.util.events.SensorDisconnectedUnexpectedlyEvent
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
    companion object {
        private const val RC_TAG = "[RECONNECT]"
    }

    private var mSession: Session? = null
    private var mErrorCallback: (() -> Unit)? = null
    private var mFinallyCallback: (() -> Unit)? = null

    private var mConnectionStatusJob: Job? = null
    private var mSyncStatusJob: Job? = null

    // Counter is kept for logging/diagnostics only — there is no hard cap on
    // reconnection attempts. The loop continues until either the device
    // reconnects, the user stops recording, sync starts, or another session
    // takes over (see observeConnectionStatus / observeSyncStatus / StopRecordingEvent).
    var mReconnectionTriesNumber: Int? = null
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
        Log.d(RC_TAG, "reconnect() called session=${session.uuid} device=${deviceItem?.id} tries=$mReconnectionTriesNumber")
        eventbus.safeRegister(this)

        if (mReconnectionTriesNumber == null) {
            // disconnecting first to make sure the connector thread is stopped correctly etc
            sendDisconnectedEvent(session)
        }

        mSession = session
        mErrorCallback = errorCallback
        mFinallyCallback = finallyCallback

        // Observers must be launched AFTER mSession is set so the initial
        // StateFlow replay can be correctly attributed to the current session.
        observeConnectionStatus()
        observeSyncStatus()

        if (deviceItem?.type == DeviceItem.Type.AIRBEAM3 || deviceItem?.type == DeviceItem.Type.AIRBEAMMINI) {
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
        Log.d(RC_TAG, "tryToReconnectPeriodically() session=${session.uuid} device=${deviceItem?.id} currentTries=$mReconnectionTriesNumber")
        if (sessionUuidByStandaloneMode[session.uuid] == true) {
            Log.e(RC_TAG, "Will not reconnect: Session is in standalone mode (session=${session.uuid})")
            return
        }
        if (mReconnectionTriesNumber != null) {
            Log.e(RC_TAG, "Will not reconnect: Reconnection already in progress (tries=$mReconnectionTriesNumber)")
            return
        }

        mReconnectionTriesNumber = 1
        Log.d(RC_TAG, "Starting reconnect cycle (attempt=1) for session=${session.uuid}")
        reconnect(session, deviceItem)
    }

    private fun reconnect(deviceItem: DeviceItem? = null) {
        try {
            Log.d(RC_TAG, "Starting AirBeamReconnectSessionService (attempt=$mReconnectionTriesNumber device=${deviceItem?.id} session=${mSession?.uuid})")
            AirBeamReconnectSessionService.startService(
                mContext,
                deviceItem,
                mSession?.uuid
            )
            eventbus.postSticky(ReconnectionEvent(mSession?.uuid, true))
        } catch (e: Exception) {
            Log.e(
                bluetoothReconnection, "$RC_TAG Attempt to start reconnection service failed (attempt=$mReconnectionTriesNumber)\n" +
                        e.stackTraceToString()
            )
        }
    }

    private fun onDiscoveryFailed() {
        Log.w(RC_TAG, "onDiscoveryFailed() tries=$mReconnectionTriesNumber")
        if (mReconnectionTriesNumber == null) {
            finalizeReconnectionWithError()
            return
        }
        mReconnectionTriesNumber = mReconnectionTriesNumber?.plus(1)
        val session = mSession ?: return
        Log.d(RC_TAG, "Scheduling retry after discovery failure: attempt=$mReconnectionTriesNumber in ${RECONNECTION_TRIES_INTERVAL}ms")
        coroutineScope.launch {
            delay(RECONNECTION_TRIES_INTERVAL)
            reconnect(session, null, mErrorCallback, mFinallyCallback)
        }
    }

    private fun sendDisconnectedEvent(session: Session) {
        val deviceId = session.deviceId
        deviceId?.let {
            Log.d(TAG, "Posting SensorDisconnectedEvent")
            eventbus.post(SensorDisconnectedUnexpectedlyEvent(deviceId, null, session.uuid))
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
    private fun observeConnectionStatus() {
        mConnectionStatusJob?.cancel()
        mConnectionStatusJob = coroutineScope.launch {
            connectionStatusFlow.filterNotNull().collect {
                val sessionUuid = mSession?.uuid ?: return@collect
                Log.v(RC_TAG, "connectionStatus emission: isConnected=${it.isConnected} sessionUUID=${it.sessionUUID} (mySession=$sessionUuid)")
                val correctSesssionConnected = it.isConnected && it.sessionUUID == sessionUuid
                if (correctSesssionConnected) {
                    Log.d(RC_TAG, "Connection succeeded for current session=$sessionUuid")
                    onConnectedSuccessful()
                }
                // Only finalize for a *different* session that is genuinely connected
                // (non-null UUID and not ours). A stale isConnected=true emission with
                // a null/empty UUID would otherwise prematurely kill the retry loop.
                else if (it.isConnected && !it.sessionUUID.isNullOrEmpty() && it.sessionUUID != sessionUuid) {
                    Log.w(RC_TAG, "Foreign session connected (${it.sessionUUID}), finalizing")
                    finalizeReconnection()
                }
            }
        }
    }
    private fun onConnectedSuccessful() {
        updateSessionStatus(mSession, Session.Status.RECORDING)
        finalizeReconnection()
    }

    private fun observeSyncStatus() {
        mSyncStatusJob?.cancel()
        mSyncStatusJob = coroutineScope.launch {
            syncStatusFlow.collect { isSyncActive ->
                Log.v(RC_TAG, "syncStatus emission: isSyncActive=$isSyncActive")
                if (isSyncActive) {
                    Log.d(RC_TAG, "Sync became active, finalizing reconnect loop")
                    finalizeReconnection()
                }
            }
        }
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamConnectionFailedEvent) {
        Log.w(RC_TAG, "AirBeamConnectionFailedEvent device=${event.deviceItem.id} currentTries=$mReconnectionTriesNumber")
        if (mReconnectionTriesNumber == null) {
            Log.w(RC_TAG, "Got connection-failed event but mReconnectionTriesNumber is null — finalizing with error")
            finalizeReconnectionWithError()
            return
        }
        mReconnectionTriesNumber = mReconnectionTriesNumber?.plus(1)
        val deviceItem = event.deviceItem
        Log.d(RC_TAG, "Scheduling retry: attempt=$mReconnectionTriesNumber in ${RECONNECTION_TRIES_INTERVAL}ms")
        coroutineScope.launch {
            delay(RECONNECTION_TRIES_INTERVAL)
            reconnect(deviceItem)
        }
    }

    private fun finalizeReconnectionWithError() {
        Log.e(
            bluetoothReconnection,
            "$RC_TAG Finalized with error. Reconnection tries: $mReconnectionTriesNumber"
        )
        mErrorCallback?.invoke()
        finalizeReconnection()
    }

    @Subscribe
    fun onMessageEvent(event: StopRecordingEvent) {
        finalizeReconnection()
    }

    private fun finalizeReconnection() {
        Log.d(RC_TAG, "Finalizing reconnection (tries=$mReconnectionTriesNumber session=${mSession?.uuid})")
        mAirBeamDiscoveryService.reset()
        mReconnectionTriesNumber = null
        mConnectionStatusJob?.cancel()
        mConnectionStatusJob = null
        mSyncStatusJob?.cancel()
        mSyncStatusJob = null
        mFinallyCallback?.invoke()
        eventbus.postSticky(ReconnectionEvent(mSession?.uuid, false))
        unregisterFromEventBus()
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamDiscoveryFailedEvent) {
        onDiscoveryFailed()
    }

    private fun unregisterFromEventBus() {
        if (eventbus.isRegistered(this)) {
            eventbus.unregister(this)
        }
    }

    class ReconnectionEvent(val sessionUuid: String?, val inProgress: Boolean = false)
}