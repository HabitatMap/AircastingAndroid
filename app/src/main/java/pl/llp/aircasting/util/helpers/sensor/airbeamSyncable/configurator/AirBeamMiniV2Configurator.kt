package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import no.nordicsemi.android.ble.BleManager
import pl.llp.aircasting.data.api.services.FixedSessionConfig
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.exceptions.AirBeamMiniV2NackError
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.util.events.NewMeasurementEvent
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.helpers.location.LocationHelper
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2.V2BleSyncOrchestrator
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2.V2SyncMeasurement
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2.V2SyncOrchestrator
import org.greenrobot.eventbus.EventBus
import kotlin.math.abs
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Date
import java.util.UUID

class AirBeamMiniV2Configurator(
    applicationContext: Context,
    private val errorHandler: ErrorHandler,
    private val settings: Settings,
    private val coroutineScope: CoroutineScope,
    private val batteryLevelFlow: MutableSharedFlow<Int>,
    private val sessionsRepository: SessionsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository,
    private val v2StateRepository: AirBeamMiniV2StateRepository,
    private val v2SyncOrchestrator: V2SyncOrchestrator,
    private val v2BleSyncOrchestrator: V2BleSyncOrchestrator,
) : BleManager(applicationContext), AirBeamBleConfigurator {

    companion object {
        val SERVICE_UUID: UUID =
            UUID.fromString("a0e1f000-0001-4b3c-8e9a-1f2d3c4b5a60")

        private val STATUS_UUID: UUID =
            UUID.fromString("a0e1f000-0002-4b3c-8e9a-1f2d3c4b5a60")
        private val COMMAND_UUID: UUID =
            UUID.fromString("a0e1f000-0003-4b3c-8e9a-1f2d3c4b5a60")
        private val RESPONSE_UUID: UUID =
            UUID.fromString("a0e1f000-0004-4b3c-8e9a-1f2d3c4b5a60")
        private val MEASUREMENT_UUID: UUID =
            UUID.fromString("a0e1f000-0005-4b3c-8e9a-1f2d3c4b5a60")
        private val SYNC_UUID: UUID =
            UUID.fromString("a0e1f000-0006-4b3c-8e9a-1f2d3c4b5a60")

        private const val OPCODE_CONTINUE_SESSION: Byte = 0x10
        private const val OPCODE_DISCARD_SESSION: Byte = 0x11
        // Legacy WiFi manual sync — firmware renamed this to `StartWiFiSync` once the BLE
        // sync path (0x16) shipped. Kept for the dormant WiFi orchestrator code path.
        private const val OPCODE_START_SYNC: Byte = 0x12
        private const val OPCODE_NEW_SESSION: Byte = 0x13
        private const val OPCODE_SET_TIME: Byte = 0x15
        // BLE-only manual sync: firmware streams stored records on the Sync characteristic
        // (same indication format as reconnect-time auto-sync), then sends Ready (0x22) on
        // the Response characteristic, then auto-clears storage + session config.
        private const val OPCODE_START_BLE_SYNC: Byte = 0x16

        const val DEFAULT_MOBILE_INTERVAL_SECONDS = 1
        const val DEFAULT_FIXED_INTERVAL_SECONDS = 60

        private const val SYNC_RECORD_SIZE = 8

        private const val STATE_IDLE: Int = 0x00
        private const val STATE_HAS_SAVED_SESSION: Int = 0x01
        private const val STATE_RUNNING: Int = 0x02
        private const val STATE_READY_TO_SYNC: Int = 0x03

        private const val RESPONSE_ACK: Int = 0x20
        private const val RESPONSE_NACK: Int = 0x21
        private const val RESPONSE_READY: Int = 0x22
        private const val RESPONSE_SYNC_INFO: Int = 0x24

        private const val NACK_STORAGE_HAS_MEASUREMENTS: Int = 0x03
        private const val NACK_INVALID_CONFIG: Int = 0x02
        private const val NACK_INVALID_WIFI_CREDENTIALS: Int = 0x05
        private const val NACK_SYNC_FAILED: Int = 0x06

        private const val SET_TIME_INTERVAL_MS = 3_600_000L
    }

    enum class DeviceState {
        IDLE, HAS_SAVED_SESSION, RUNNING, READY_TO_SYNC, UNKNOWN
    }

    enum class CommandState {
        IDLE, WAITING_ACK, WAITING_READY
    }

    private var statusCharacteristic: BluetoothGattCharacteristic? = null
    private var commandCharacteristic: BluetoothGattCharacteristic? = null
    private var responseCharacteristic: BluetoothGattCharacteristic? = null
    private var measurementCharacteristic: BluetoothGattCharacteristic? = null
    private var syncCharacteristic: BluetoothGattCharacteristic? = null

    private var setTimeJob: Job? = null
    private var commandState: CommandState = CommandState.IDLE
    private var sessionReadyDeferred: CompletableDeferred<Boolean>? = null
    private var lastNackCode: Int = -1
    private var pendingMobileReconnect: Boolean = false
    private var isCurrentSessionFixed: Boolean = false
    // Firmware emits Ready (0x22) on every successful measurement POST while BLE is connected,
    // not only after BLE setup. Setup work (e.g. hourly SetTime) must run on the first Ready only.
    private var sessionReadyHandled: Boolean = false
    private var fixedConfigInFlight: Boolean = false
    // Set true when the next expected Ready means "session has started" (NewSessionConfig
    // or ContinueSession). DiscardSession and StartSync also emit Ready, but those mean
    // "procedure done" — they must not trigger session-start hooks.
    private var awaitingSessionStartReady: Boolean = false

    var deviceId: String? = null
    private var lastBluetoothDevice: android.bluetooth.BluetoothDevice? = null
    var currentState: DeviceState = DeviceState.UNKNOWN
        private set
    var currentBatteryLevel: Int = -1
        private set
    var savedSessionUuid: ByteArray? = null
        private set
    var hasSavedMeasurements: Boolean = false
        private set
    // Byte length of the unsynced measurements stored on the device, parsed from the
    // HasSavedSession Status payload suffix (FW commit `3990cf22`). 0 when the device
    // reports no measurements, runs older firmware, or fails to read storage metadata.
    var savedSessionFileSize: Long = 0L
        private set

    /**
     * When set, sync-characteristic indications are routed here instead of being saved
     * directly to the active mobile session in the DB. Used by [V2BleSyncOrchestrator] to
     * collect stored records during a manual BLE sync (StartBleSync 0x16) so the orchestrator
     * can route them to the correct destination (mobile DB insert / fixed-session POST).
     */
    private var manualSyncChunkHandler: ((List<V2SyncMeasurement>) -> Unit)? = null

    fun setManualSyncChunkHandler(handler: ((List<V2SyncMeasurement>) -> Unit)?) {
        manualSyncChunkHandler = handler
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(SERVICE_UUID) ?: return false

        statusCharacteristic = service.getCharacteristic(STATUS_UUID)
        commandCharacteristic = service.getCharacteristic(COMMAND_UUID)
        responseCharacteristic = service.getCharacteristic(RESPONSE_UUID)
        measurementCharacteristic = service.getCharacteristic(MEASUREMENT_UUID)
        syncCharacteristic = service.getCharacteristic(SYNC_UUID)

        val hasNotify = { c: BluetoothGattCharacteristic? ->
            c != null && (c.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
        }
        val hasIndicate = { c: BluetoothGattCharacteristic? ->
            c != null && (c.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0
        }
        val hasWrite = { c: BluetoothGattCharacteristic? ->
            c != null && (c.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0
        }

        return hasNotify(statusCharacteristic) &&
                hasWrite(commandCharacteristic) &&
                hasNotify(responseCharacteristic) &&
                hasIndicate(measurementCharacteristic) &&
                hasIndicate(syncCharacteristic)
    }

    override fun initialize() {
        requestMtu(247)
            .with { _, mtu -> Log.d(TAG, "V2: MTU negotiated: $mtu") }
            .fail { _, status -> Log.w(TAG, "V2: MTU request failed, status=$status") }
            .enqueue()

        val queue = beginAtomicRequestQueue()

        // Status notifications
        setNotificationCallback(statusCharacteristic).with { _, data ->
            val bytes = data.value ?: return@with
            parseStatus(bytes)
        }
        queue.add(
            enableNotifications(statusCharacteristic)
                .fail { _, status -> logError("status notification", status) }
        ).add(sleep(300))

        // Response notifications
        setNotificationCallback(responseCharacteristic).with { _, data ->
            val bytes = data.value ?: return@with
            parseResponse(bytes)
        }
        queue.add(
            enableNotifications(responseCharacteristic)
                .fail { _, status -> logError("response notification", status) }
        ).add(sleep(300))

        // Measurement indications
        setNotificationCallback(measurementCharacteristic).with { _, data ->
            val bytes = data.value ?: return@with
            parseMeasurement(bytes)
        }
        queue.add(
            enableIndications(measurementCharacteristic)
                .fail { _, status -> logError("measurement indication", status) }
        ).add(sleep(300))

        // Sync indications (historical measurements streamed on reconnect)
        setNotificationCallback(syncCharacteristic).with { _, data ->
            val bytes = data.value ?: return@with
            parseSyncChunk(bytes)
        }
        queue.add(
            enableIndications(syncCharacteristic)
                .fail { _, status -> logError("sync indication", status) }
        )

        // Read Status characteristic explicitly — firmware sends the notification at ~300ms after
        // connection, but Android service discovery often takes longer, so the notification is
        // missed. The firmware stores the value via set_value(), so a READ always returns it.
        queue.add(
            readCharacteristic(statusCharacteristic)
                .with { _, data ->
                    val bytes = data.value ?: return@with
                    if (currentState == DeviceState.UNKNOWN) parseStatus(bytes)
                }
                .fail { _, status -> Log.w(TAG, "V2: Status read failed, status=$status") }
        )

        queue.enqueue()

        // Send initial SetTime after subscriptions settle
        sendSetTime()

        // Manual sync flow used by `SyncBeforeNewV2SessionDialog` and the SD-sync entry point.
        // Catch here so an orchestrator failure can never crash the calling Activity scope.
        // Now BLE-based (StartBleSync 0x16); the legacy WiFi orchestrator [V2SyncOrchestrator]
        // is kept in the codebase but no longer invoked.
        v2StateRepository.setSyncCallback { keepConnectedAfter, onBeforePicker ->
            try {
                v2BleSyncOrchestrator.run(this, keepConnectedAfter, onBeforePicker)
            } catch (e: Exception) {
                Log.e(TAG, "V2: sync orchestrator threw", e)
                false
            }
        }
    }

    override fun onServicesInvalidated() {
        setTimeJob?.cancel()
        statusCharacteristic = null
        commandCharacteristic = null
        responseCharacteristic = null
        measurementCharacteristic = null
        syncCharacteristic = null
    }

    override fun log(priority: Int, message: String) {
        Log.println(priority, TAG, message)
    }

    // -- AirBeamBleConfigurator bridge methods --

    override fun setObserver(observer: no.nordicsemi.android.ble.observer.ConnectionObserver) {
        connectionObserver = observer
    }

    override fun connectDevice(device: android.bluetooth.BluetoothDevice): no.nordicsemi.android.ble.ConnectRequest {
        lastBluetoothDevice = device
        return connect(device)
    }

    override fun closeConnection() {
        close()
    }

    /**
     * Voluntary GATT disconnect that keeps the [BleManager] alive for a later [connect].
     * Used by the V2 sync orchestrator to free the phone radio for the SoftAP HTTP transfer
     * without tearing down the manager. `useAutoConnect(true)` from the initial connection
     * does not retrigger because Nordic's `disconnect().enqueue()` is treated as voluntary.
     */
    suspend fun disconnectGattForSync(): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        disconnect()
            .done {
                Log.d(TAG, "V2: BLE voluntary disconnect for sync done")
                deferred.complete(true)
            }
            .fail { _, status ->
                Log.w(TAG, "V2: BLE voluntary disconnect failed, status=$status")
                deferred.complete(false)
            }
            .enqueue()
        return withTimeoutOrNull(3_000L) { deferred.await() } ?: false
    }

    /**
     * Re-establish the GATT connection that [disconnectGattForSync] dropped, send
     * `DiscardSession (0x11)`, await `Ready (0x22)`, then voluntarily disconnect again unless
     * [keepConnectedAfter] is set (used by the new-session flow which needs to write
     * `NewSessionConfig` on the same GATT link right after).
     * Reused on the same [BleManager] instance so [initialize] re-runs and characteristic
     * references are repopulated cleanly.
     */
    suspend fun reconnectAndSendDiscard(keepConnectedAfter: Boolean = false): Boolean {
        val device = lastBluetoothDevice ?: run {
            Log.w(TAG, "V2: reconnectAndSendDiscard skipped — no cached BluetoothDevice")
            return false
        }
        val connected = CompletableDeferred<Boolean>()
        connect(device)
            .timeout(15_000L)
            .useAutoConnect(false)
            .done {
                Log.d(TAG, "V2: BLE reconnected for Discard")
                connected.complete(true)
            }
            .fail { _, status ->
                Log.w(TAG, "V2: BLE reconnect for Discard failed, status=$status")
                connected.complete(false)
            }
            .enqueue()

        val ok = withTimeoutOrNull(20_000L) { connected.await() } ?: false
        if (!ok) return false
        val discarded = sendDiscardSessionAndAwait()
        if (keepConnectedAfter) {
            Log.d(TAG, "V2: post-sync Discard ok=$discarded — keeping BLE connected for caller")
        } else {
            Log.d(TAG, "V2: post-sync Discard ok=$discarded — voluntary disconnect")
            runCatching { disconnectGattForSync() }
        }
        return discarded
    }

    override fun discardSession() {
        val cmd = commandCharacteristic ?: run {
            Log.d(TAG, "V2: discardSession skipped (command characteristic null)")
            return
        }

        runBlocking {
            val deferred = CompletableDeferred<Boolean>()
            sessionReadyDeferred = deferred
            commandState = CommandState.WAITING_ACK
            awaitingSessionStartReady = false

            writeCharacteristic(cmd, byteArrayOf(OPCODE_DISCARD_SESSION), WRITE_TYPE_DEFAULT)
                .fail { _, status ->
                    Log.e(TAG, "V2: DiscardSession write failed, status=$status")
                    commandState = CommandState.IDLE
                    deferred.complete(false)
                }
                .enqueue()

            Log.d(TAG, "V2: DiscardSession sent on session finish, awaiting Ready")
            val result = withTimeoutOrNull(3_000L) { deferred.await() }
            if (result == null) {
                Log.w(TAG, "V2: DiscardSession Ready timeout — proceeding to close")
                commandState = CommandState.IDLE
            } else {
                Log.d(TAG, "V2: DiscardSession completed=$result")
            }
        }
    }

    override fun sendAuth(uuid: String) {
        // V2 has no auth handshake — no-op
        Log.d(TAG, "V2: sendAuth called (no-op, V2 has no auth)")
    }

    override fun configure(session: Session, wifiSSID: String?, wifiPassword: String?, fixedSessionConfig: FixedSessionConfig?, intervalSeconds: Int?) {
        if (deviceId == null) deviceId = session.deviceId
        isCurrentSessionFixed = session.isFixed()
        sessionReadyHandled = false
        fixedConfigInFlight = isCurrentSessionFixed
        awaitingSessionStartReady = false

        if (commandCharacteristic == null) {
            Log.e(TAG, "V2: Command characteristic not available")
            emitFixedFailureIfApplicable(FixedSessionConfigureOutcome.Reason.WRITE_FAILED)
            return
        }

        if (session.isFixed() && fixedSessionConfig == null) {
            Log.e(TAG, "V2: Fixed session but fixedSessionConfig is null (upload failed or backend returned no token/streams). Aborting configure.")
            emitFixedFailureIfApplicable(FixedSessionConfigureOutcome.Reason.OTHER_NACK)
            return
        }

        if (session.isFixed() && fixedSessionConfig != null) {
            coroutineScope.launch {
                val discarded = discardSavedSessionAndAwait()
                if (discarded) {
                    sendNewSessionConfig(session, wifiSSID, wifiPassword, fixedSessionConfig, intervalSeconds)
                } else {
                    Log.e(TAG, "V2: DiscardSession failed, aborting NewSessionConfig")
                    emitFixedFailureIfApplicable(FixedSessionConfigureOutcome.Reason.WRITE_FAILED)
                }
            }
        } else if (currentState == DeviceState.HAS_SAVED_SESSION) {
            // Device has a saved session from before; discard it before starting a new mobile session.
            coroutineScope.launch {
                val discarded = discardSavedSessionAndAwait()
                if (discarded) {
                    sendNewSessionConfig(session, wifiSSID, wifiPassword, fixedSessionConfig, intervalSeconds)
                } else {
                    Log.e(TAG, "V2: DiscardSession failed for mobile session, aborting")
                }
            }
        } else {
            sendNewSessionConfig(session, wifiSSID, wifiPassword, fixedSessionConfig, intervalSeconds)
        }
    }

    private fun emitFixedFailureIfApplicable(reason: FixedSessionConfigureOutcome.Reason, errorCode: Int = -1) {
        if (!fixedConfigInFlight) return
        fixedConfigInFlight = false
        v2StateRepository.emitConfigureOutcome(FixedSessionConfigureOutcome.Failure(reason, errorCode))
    }

    private fun emitFixedSuccessIfApplicable() {
        if (!fixedConfigInFlight) return
        fixedConfigInFlight = false
        v2StateRepository.emitConfigureOutcome(FixedSessionConfigureOutcome.Success)
    }

    private suspend fun discardSavedSessionAndAwait(): Boolean {
        val cmd = commandCharacteristic ?: return false
        val deferred = CompletableDeferred<Boolean>()
        sessionReadyDeferred = deferred
        commandState = CommandState.WAITING_ACK

        writeCharacteristic(cmd, byteArrayOf(OPCODE_DISCARD_SESSION), WRITE_TYPE_DEFAULT)
            .fail { _, status ->
                Log.e(TAG, "V2: DiscardSession write failed, status=$status")
                commandState = CommandState.IDLE
                deferred.complete(false)
            }
            .enqueue()

        Log.d(TAG, "V2: DiscardSession sent")
        return deferred.await()
    }

    private fun sendNewSessionConfig(
        session: Session,
        wifiSSID: String?,
        wifiPassword: String?,
        fixedSessionConfig: FixedSessionConfig?,
        intervalSeconds: Int?,
    ) {
        val cmd = commandCharacteristic ?: return
        val payload = if (fixedSessionConfig != null && wifiSSID != null && wifiPassword != null) {
            val pm1Index = fixedSessionConfig.sensorTypeIds["AirBeamMini-PM1"] ?: 0
            val pm25Index = fixedSessionConfig.sensorTypeIds["AirBeamMini-PM2.5"] ?: 1
            val interval = intervalSeconds ?: DEFAULT_FIXED_INTERVAL_SECONDS
            buildFixedSessionPayload(session.uuid, fixedSessionConfig.sessionToken, pm1Index, pm25Index, wifiSSID, wifiPassword, interval)
        } else {
            val interval = intervalSeconds ?: DEFAULT_MOBILE_INTERVAL_SECONDS
            buildMobileSessionPayload(session.uuid, interval)
        }

        commandState = CommandState.WAITING_ACK
        sessionReadyDeferred = CompletableDeferred()
        awaitingSessionStartReady = true

        writeCharacteristic(cmd, payload, WRITE_TYPE_DEFAULT)
            .fail { _, status ->
                Log.e(TAG, "V2: NewSessionConfig write failed, status=$status")
                commandState = CommandState.IDLE
                awaitingSessionStartReady = false
                sessionReadyDeferred?.complete(false)
                emitFixedFailureIfApplicable(FixedSessionConfigureOutcome.Reason.WRITE_FAILED)
            }
            .enqueue()

        Log.d(TAG, "V2: NewSessionConfig sent (${payload.size} bytes), uuid=${session.uuid}")
    }

    override fun reconnectMobileSession() {
        sendSetTime()

        when (currentState) {
            DeviceState.RUNNING -> {
                startHourlySetTime()
                Log.d(TAG, "V2: Device Running, sync + live measurements flowing")
            }

            DeviceState.HAS_SAVED_SESSION -> {
                coroutineScope.launch { sendContinueSession() }
                Log.d(TAG, "V2: HasSavedSession, sending ContinueSession")
            }

            DeviceState.UNKNOWN -> {
                // Status notification not yet received — defer until parseStatus fires
                pendingMobileReconnect = true
                Log.d(TAG, "V2: State unknown on reconnect, deferring until Status arrives")
            }

            else -> {
                Log.w(TAG, "V2: Unexpected state on reconnect: $currentState")
            }
        }
    }

    private fun handlePendingReconnect() {
        pendingMobileReconnect = false
        when (currentState) {
            DeviceState.RUNNING -> {
                startHourlySetTime()
                Log.d(TAG, "V2: Deferred reconnect — Device Running, sync + live measurements flowing")
            }
            DeviceState.HAS_SAVED_SESSION -> {
                coroutineScope.launch { sendContinueSession() }
                Log.d(TAG, "V2: Deferred reconnect — HasSavedSession, sending ContinueSession")
            }
            else -> {
                Log.w(TAG, "V2: Deferred reconnect — unexpected state: $currentState")
            }
        }
    }

    override fun triggerSDCardDownload() {
        // V2 has no SD card — the SD-sync entry point reuses the manual BLE-sync flow.
        // Run on the BLE coroutine scope so the suspending orchestrator can await BLE responses.
        Log.d(TAG, "V2: triggerSDCardDownload routing to V2 BLE manual sync orchestrator")
        coroutineScope.launch {
            try {
                val ok = v2BleSyncOrchestrator.run(this@AirBeamMiniV2Configurator)
                Log.d(TAG, "V2: triggerSDCardDownload orchestrator finished ok=$ok")
            } catch (e: Exception) {
                Log.e(TAG, "V2: triggerSDCardDownload orchestrator threw", e)
            }
        }
    }

    override suspend fun clearSDCard() {
        // V2 does not use SD card
        Log.d(TAG, "V2: clearSDCard called (no-op)")
    }

    override fun reset() {
        Log.d(TAG, "V2: Resetting")
        setTimeJob?.cancel()
        commandState = CommandState.IDLE
        sessionReadyDeferred?.cancel()
        sessionReadyDeferred = null
        pendingMobileReconnect = false
        isCurrentSessionFixed = false
        sessionReadyHandled = false
        fixedConfigInFlight = false
        awaitingSessionStartReady = false
        statusCharacteristic = null
        commandCharacteristic = null
        responseCharacteristic = null
        measurementCharacteristic = null
        syncCharacteristic = null
        v2StateRepository.reset()
    }

    // -- SetTime --

    fun sendSetTime() {
        val cmd = commandCharacteristic ?: return
        val epochSeconds = System.currentTimeMillis() / 1000
        val buffer = ByteBuffer.allocate(9).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(OPCODE_SET_TIME)
        buffer.putLong(epochSeconds)

        writeCharacteristic(cmd, buffer.array(), WRITE_TYPE_DEFAULT)
            .fail { _, status -> logError("SetTime", status) }
            .enqueue()

        Log.d(TAG, "V2: SetTime sent (epoch=$epochSeconds)")
    }

    fun startHourlySetTime() {
        setTimeJob?.cancel()
        setTimeJob = coroutineScope.launch {
            while (isActive) {
                delay(SET_TIME_INTERVAL_MS)
                sendSetTime()
            }
        }
    }

    fun stopHourlySetTime() {
        setTimeJob?.cancel()
        setTimeJob = null
    }

    // -- Status parsing --

    private fun parseStatus(bytes: ByteArray) {
        if (bytes.isEmpty()) return

        val state = bytes[0].toInt() and 0xFF

        // ReadyToSync (0x03) has a different payload shape than the rest of the Status
        // characteristic: [0x03, file_size_u64_LE (8B), utf8 password bytes...].
        // FW commit `ed751b180` added the file_size prefix so the app can size the WiFi
        // download (progress UI). No battery byte — short-circuit before generic battery
        // parsing.
        if (state == STATE_READY_TO_SYNC) {
            if (bytes.size < 9) {
                Log.w(TAG, "V2 Status: ReadyToSync payload too short (${bytes.size}B), expected ≥9")
                return
            }
            val fileSize = ByteBuffer.wrap(bytes, 1, 8).order(ByteOrder.LITTLE_ENDIAN).long
            val passwordBytes = if (bytes.size > 9) bytes.copyOfRange(9, bytes.size) else ByteArray(0)
            val password = String(passwordBytes, Charsets.UTF_8)
            currentState = DeviceState.READY_TO_SYNC
            val rawHex = bytes.joinToString("") { "%02x".format(it) }
            val pwHex = passwordBytes.joinToString("") { "%02x".format(it) }
            Log.d(
                TAG,
                "V2 Status: ReadyToSync — fileSize=$fileSize passwordLen=${password.length} " +
                        "password='$password' passwordHex=$pwHex rawStatusHex=$rawHex",
            )
            v2StateRepository.update(currentState, hasSavedMeasurements, savedSessionUuid?.let { leBytesToUuid(it) }, savedSessionFileSize)
            v2StateRepository.emitReadyToSyncFileSize(fileSize)
            v2StateRepository.emitReadyToSyncPassword(password)
            return
        }

        if (bytes.size < 2) return

        // FW encodes charging direction via sign: positive = charging, negative = discharging.
        // Cast to i8 first, then abs() for the actual level.
        val signedBattery = bytes[1].toInt()
        val battery = abs(signedBattery)
        val isCharging = signedBattery > 0
        currentBatteryLevel = battery

        coroutineScope.launch { batteryLevelFlow.emit(battery) }

        when (state) {
            STATE_IDLE -> {
                currentState = DeviceState.IDLE
                savedSessionUuid = null
                hasSavedMeasurements = false
                savedSessionFileSize = 0L
                Log.d(TAG, "V2 Status: Idle, battery=$battery%, charging=$isCharging")
            }

            STATE_HAS_SAVED_SESSION -> {
                currentState = DeviceState.HAS_SAVED_SESSION
                if (bytes.size >= 19) {
                    savedSessionUuid = bytes.copyOfRange(2, 18)
                    hasSavedMeasurements = bytes[18].toInt() != 0
                }
                // FW commit `3990cf22` appends an 8B `file_size_u64_LE` after the
                // has_measurements byte (payload grows from 19 → 27 bytes). Older
                // firmware omits it — fall back to 0 so the ETA UI is skipped.
                savedSessionFileSize = if (bytes.size >= 27) {
                    ByteBuffer.wrap(bytes, 19, 8).order(ByteOrder.LITTLE_ENDIAN).long
                } else {
                    0L
                }
                Log.d(TAG, "V2 Status: HasSavedSession, battery=$battery%, charging=$isCharging, hasMeasurements=$hasSavedMeasurements, fileSize=$savedSessionFileSize")
            }

            STATE_RUNNING -> {
                currentState = DeviceState.RUNNING
                // Byte 18 present when device is actively streaming sync data; absent otherwise.
                // Firmware re-notifies with has_measurements=false when sync completes.
                if (bytes.size >= 19) {
                    savedSessionUuid = bytes.copyOfRange(2, 18)
                    hasSavedMeasurements = bytes[18].toInt() != 0
                } else if (bytes.size >= 18) {
                    savedSessionUuid = bytes.copyOfRange(2, 18)
                    hasSavedMeasurements = false
                }
                Log.d(TAG, "V2 Status: Running, battery=$battery%, charging=$isCharging, hasMeasurements=$hasSavedMeasurements")
            }

            else -> {
                currentState = DeviceState.UNKNOWN
                Log.w(TAG, "V2 Status: Unknown state 0x${state.toString(16)}, battery=$battery%, charging=$isCharging")
            }
        }

        v2StateRepository.update(currentState, hasSavedMeasurements, savedSessionUuid?.let { leBytesToUuid(it) }, savedSessionFileSize)

        if (pendingMobileReconnect) handlePendingReconnect()
    }

    // -- Response parsing --

    private fun parseResponse(bytes: ByteArray) {
        if (bytes.isEmpty()) return
        val opcode = bytes[0].toInt() and 0xFF
        Log.d(TAG, "V2 Response: opcode=0x${"%02x".format(opcode)}, state=$commandState")

        when (opcode) {
            RESPONSE_ACK -> {
                if (commandState == CommandState.WAITING_ACK) {
                    commandState = CommandState.WAITING_READY
                    Log.d(TAG, "V2: Ack received, waiting for Ready")
                }
            }

            RESPONSE_NACK -> {
                val errorCode = if (bytes.size > 1) bytes[1].toInt() and 0xFF else -1
                Log.e(TAG, "V2: Nack received, error code=$errorCode")
                lastNackCode = errorCode
                commandState = CommandState.IDLE
                awaitingSessionStartReady = false
                sessionReadyDeferred?.complete(false)
                if (errorCode != NACK_STORAGE_HAS_MEASUREMENTS) {
                    val nackError = AirBeamMiniV2NackError(errorCode)
                    if (fixedConfigInFlight) {
                        // Controller observes the typed outcome and shows a single dialog;
                        // log to crashlytics here without surfacing a duplicate generic dialog.
                        errorHandler.handle(nackError)
                    } else {
                        errorHandler.handleAndDisplay(nackError)
                    }
                }

                if (fixedConfigInFlight) {
                    val reason = when (errorCode) {
                        NACK_INVALID_WIFI_CREDENTIALS -> FixedSessionConfigureOutcome.Reason.INVALID_WIFI_CREDENTIALS
                        NACK_INVALID_CONFIG -> FixedSessionConfigureOutcome.Reason.FIRST_MEASUREMENT_FAILED
                        else -> FixedSessionConfigureOutcome.Reason.OTHER_NACK
                    }
                    emitFixedFailureIfApplicable(reason, errorCode)
                }
            }

            RESPONSE_READY -> {
                if (commandState == CommandState.WAITING_READY) {
                    Log.d(TAG, "V2: Ready received")
                    commandState = CommandState.IDLE
                    sessionReadyDeferred?.complete(true)
                    if (awaitingSessionStartReady) {
                        awaitingSessionStartReady = false
                        sessionReadyHandled = true
                        onSessionReady()
                    }
                } else if (sessionReadyHandled) {
                    // Firmware emits Ready after every measurement POST while BLE is connected.
                    Log.d(TAG, "V2: Ready heartbeat (per-measurement)")
                } else {
                    Log.w(TAG, "V2: Ready received in unexpected state $commandState")
                }
            }

            RESPONSE_SYNC_INFO -> Log.d(TAG, "V2: SyncInfo received (${bytes.size - 1} bytes)")

            else -> Log.w(TAG, "V2: Unknown response 0x${"%02x".format(opcode)}")
        }
    }

    private fun onSessionReady() {
        // Fixed sessions receive time updates from backend via X-Server-Time header
        // on each WiFi POST, so hourly BLE SetTime is only needed for mobile sessions.
        if (!isCurrentSessionFixed) startHourlySetTime()

        // First Ready proves the configure flow succeeded — for fixed sessions, firmware
        // emits Ready only after a successful measurement POST, so this is a real ack.
        emitFixedSuccessIfApplicable()
    }

    // -- Session config payload --

    private fun buildMobileSessionPayload(sessionUuid: String, intervalSeconds: Int): ByteArray {
        // Mobile: 0x13 + 16B_UUID + 2B_interval(u16) + 0x01 = 20 bytes
        val buffer = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(OPCODE_NEW_SESSION)
        buffer.put(uuidToLeBytes(sessionUuid))
        buffer.putShort(intervalSeconds.toShort())
        buffer.put(0x01)   // mobile mode
        return buffer.array()
    }

    /**
     * Fixed session payload (134 bytes), matching firmware ble_protocol.rs layout:
     * 0x13 (1B) + UUID_LE (16B) + interval_u16_LE (2B) + mode=0x00 (1B) +
     * pm1_index (1B) + pm25_index (1B) + session_token (16B) + SSID_padded (32B) + password_padded (64B)
     *
     * Note: interval and mode come BEFORE the indices and token — byte 19 is the mode byte,
     * which is what the firmware reads to distinguish MOBILE (0x01) from FIXED (0x00).
     *
     * session_token: 16 bytes decoded from the backend's 32-char hex string.
     */
    private fun buildFixedSessionPayload(
        sessionUuid: String,
        sessionToken: ByteArray,
        pm1Index: Int,
        pm25Index: Int,
        wifiSSID: String,
        wifiPassword: String,
        intervalSeconds: Int,
    ): ByteArray {
        val ssidBytes = wifiSSID.toByteArray(Charsets.UTF_8).copyOf(32)
        val passBytes = wifiPassword.toByteArray(Charsets.UTF_8).copyOf(64)
        val buffer = ByteBuffer.allocate(134).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(OPCODE_NEW_SESSION)
        buffer.put(uuidToLeBytes(sessionUuid))
        buffer.putShort(intervalSeconds.toShort())
        buffer.put(0x00)                // fixed mode (byte 19 — firmware reads this as session_type)
        buffer.put(pm1Index.toByte())
        buffer.put(pm25Index.toByte())
        // Firmware reads token as u128::from_le_bytes, so reverse the BE hex byte order.
        buffer.put(sessionToken.reversedArray()) // 16B token (bytes 22-37), little-endian
        buffer.put(ssidBytes)
        buffer.put(passBytes)
        return buffer.array()
    }

    /**
     * Convert a UUID string to 16-byte little-endian format as expected by firmware
     * (Uuid::from_slice_le). First three groups are byte-reversed, last 8 bytes unchanged.
     */
    private fun uuidToLeBytes(uuidString: String): ByteArray {
        val uuid = UUID.fromString(uuidString)
        val msb = uuid.mostSignificantBits
        val lsb = uuid.leastSignificantBits

        // Standard big-endian byte order first
        val std = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN)
            .putLong(msb)
            .putLong(lsb)
            .array()

        // Reverse first 3 groups for LE encoding
        return byteArrayOf(
            std[3], std[2], std[1], std[0],  // time_low (4B reversed)
            std[5], std[4],                    // time_mid (2B reversed)
            std[7], std[6],                    // time_hi_and_version (2B reversed)
            std[8], std[9], std[10], std[11],  // clock_seq + node (unchanged)
            std[12], std[13], std[14], std[15]
        )
    }

    // -- Measurement parsing --

    private fun parseMeasurement(bytes: ByteArray) {
        if (bytes.size < 9) {
            Log.w(TAG, "V2: Measurement too short: ${bytes.size} bytes")
            return
        }

        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val count = buffer.get().toInt() and 0xFF
        val timestamp = buffer.getInt().toLong() and 0xFFFFFFFFL
        val pm1 = buffer.getShort().toInt() and 0xFFFF
        val pm25 = buffer.getShort().toInt() and 0xFFFF

        Log.d(TAG, "V2: Measurement count=$count, ts=$timestamp, PM1=$pm1, PM2.5=$pm25")

        val devId = deviceId ?: run {
            Log.e(TAG, "V2: deviceId null when live measurement arrived")
            return
        }

        val time = Date(timestamp * 1000)
        val location = Session.Location.get(LocationHelper.lastLocation(), settings.areMapsDisabled())
        coroutineScope.launch {
            try {
                saveLiveMeasurementToDb(devId, listOf(Measurement(pm1.toDouble(), time, location.latitude, location.longitude)), listOf(Measurement(pm25.toDouble(), time, location.latitude, location.longitude)))
            } catch (e: Exception) {
                Log.e(TAG, "V2: saveLiveMeasurementToDb EXCEPTION", e)
            }
        }
    }

    // -- ContinueSession --

    private suspend fun sendContinueSession() {
        val cmd = commandCharacteristic ?: run {
            Log.e(TAG, "V2: Command characteristic not available for ContinueSession")
            return
        }

        lastNackCode = -1
        commandState = CommandState.WAITING_ACK
        val deferred = CompletableDeferred<Boolean>()
        sessionReadyDeferred = deferred
        awaitingSessionStartReady = true

        writeCharacteristic(cmd, byteArrayOf(OPCODE_CONTINUE_SESSION), WRITE_TYPE_DEFAULT)
            .fail { _, status ->
                Log.e(TAG, "V2: ContinueSession write failed, status=$status")
                commandState = CommandState.IDLE
                awaitingSessionStartReady = false
                deferred.complete(false)
            }
            .enqueue()

        Log.d(TAG, "V2: ContinueSession sent")
        val success = deferred.await()

        if (!success && lastNackCode == NACK_STORAGE_HAS_MEASUREMENTS) {
            Log.d(TAG, "V2: ContinueSession rejected (unsynced measurements), sending StartSync first")
            val syncSuccess = sendStartSyncAndAwait()
            if (syncSuccess) {
                Log.d(TAG, "V2: StartSync complete, retrying ContinueSession")
                lastNackCode = -1
                commandState = CommandState.WAITING_ACK
                val retryDeferred = CompletableDeferred<Boolean>()
                sessionReadyDeferred = retryDeferred
                awaitingSessionStartReady = true
                writeCharacteristic(cmd, byteArrayOf(OPCODE_CONTINUE_SESSION), WRITE_TYPE_DEFAULT)
                    .fail { _, status ->
                        Log.e(TAG, "V2: ContinueSession retry write failed, status=$status")
                        commandState = CommandState.IDLE
                        awaitingSessionStartReady = false
                        retryDeferred.complete(false)
                    }
                    .enqueue()
                retryDeferred.await()
            } else {
                Log.e(TAG, "V2: StartSync failed, cannot resume session")
                errorHandler.showError("Failed to sync device storage before resuming session.")
            }
        }
    }

    private suspend fun sendStartSyncAndAwait(): Boolean {
        val cmd = commandCharacteristic ?: return false
        commandState = CommandState.WAITING_ACK
        val deferred = CompletableDeferred<Boolean>()
        sessionReadyDeferred = deferred

        writeCharacteristic(cmd, byteArrayOf(OPCODE_START_SYNC), WRITE_TYPE_DEFAULT)
            .fail { _, status ->
                Log.e(TAG, "V2: StartSync write failed, status=$status")
                commandState = CommandState.IDLE
                deferred.complete(false)
            }
            .enqueue()

        Log.d(TAG, "V2: StartSync sent")
        return deferred.await()
    }

    /**
     * Send `StartSync (0x12)` and wait until firmware finishes the manual-sync flow:
     * Ack → ReadyToSync(password) on Status (handled separately by orchestrator) → Ready (0x22).
     * Returns true on Ready, false on Nack / write failure.
     */
    suspend fun sendStartSyncManualAndAwaitDone(): Boolean = sendStartSyncAndAwait()

    /**
     * Send `StartBleSync (0x16)` and await the firmware-driven BLE sync flow:
     * Ack (0x20) → ReadyToSync (Status 0x03, file_size only; password ignored on BLE path) →
     * batched indications on Sync characteristic → Ready (0x22) on Response.
     * Returns true on Ready, false on Nack (0x06 SyncFailed / others) or write failure.
     */
    suspend fun sendStartBleSyncAndAwaitDone(): Boolean {
        val cmd = commandCharacteristic ?: return false
        commandState = CommandState.WAITING_ACK
        val deferred = CompletableDeferred<Boolean>()
        sessionReadyDeferred = deferred

        writeCharacteristic(cmd, byteArrayOf(OPCODE_START_BLE_SYNC), WRITE_TYPE_DEFAULT)
            .fail { _, status ->
                Log.e(TAG, "V2: StartBleSync write failed, status=$status")
                commandState = CommandState.IDLE
                deferred.complete(false)
            }
            .enqueue()

        Log.d(TAG, "V2: StartBleSync sent")
        return deferred.await()
    }

    /**
     * After-the-fact wrapper exposed for the V2 sync orchestrator: write 0x11 (DiscardSession)
     * and block on the resulting Ack→Ready cycle. Returns true on Ready.
     */
    suspend fun sendDiscardSessionAndAwait(): Boolean = discardSavedSessionAndAwait()

    // -- Sync chunk parsing & DB saving --

    private fun parseSyncChunk(bytes: ByteArray) {
        Log.d(TAG, "V2: Sync callback fired, ${bytes.size} bytes, deviceId=$deviceId, raw=${bytes.joinToString(" ") { "%02x".format(it) }}")

        if (bytes.size < 3) {
            Log.w(TAG, "V2: Sync chunk too short: ${bytes.size} bytes")
            return
        }

        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val count = buffer.get().toInt() and 0xFF
        buffer.position(3) // skip 2 padding bytes

        val expectedSize = 3 + count * SYNC_RECORD_SIZE
        if (bytes.size < expectedSize) {
            Log.w(TAG, "V2: Sync chunk too short for $count records: ${bytes.size} < $expectedSize bytes")
            return
        }

        // Manual BLE sync (StartBleSync 0x16): collect records for the orchestrator and
        // skip the direct-to-DB save path used by reconnect-time auto-sync.
        val manualHandler = manualSyncChunkHandler
        if (manualHandler != null) {
            val records = ArrayList<V2SyncMeasurement>(count)
            for (i in 0 until count) {
                val ts = buffer.getInt().toLong() and 0xFFFFFFFFL
                val pm1 = buffer.getShort().toInt() and 0xFFFF
                val pm25 = buffer.getShort().toInt() and 0xFFFF
                records.add(V2SyncMeasurement(Date(ts * 1000), pm1, pm25))
            }
            Log.d(TAG, "V2: Sync chunk routed to manual handler ($count records)")
            manualHandler(records)
            return
        }

        val devId = deviceId ?: run {
            Log.e(TAG, "V2: deviceId is null when sync chunk arrived, cannot save")
            return
        }
        val pm1Measurements = mutableListOf<Measurement>()
        val pm25Measurements = mutableListOf<Measurement>()

        val location = Session.Location.get(LocationHelper.lastLocation(), settings.areMapsDisabled())

        for (i in 0 until count) {
            val timestamp = buffer.getInt().toLong() and 0xFFFFFFFFL
            val pm1 = buffer.getShort().toInt() and 0xFFFF
            val pm25 = buffer.getShort().toInt() and 0xFFFF
            val time = Date(timestamp * 1000)

            pm1Measurements.add(Measurement(pm1.toDouble(), time, location.latitude, location.longitude))
            pm25Measurements.add(Measurement(pm25.toDouble(), time, location.latitude, location.longitude))
        }

        Log.d(TAG, "V2: Sync chunk parsed: $count records, first ts=${pm1Measurements.firstOrNull()?.time}")

        coroutineScope.launch {
            try {
                saveSyncChunkToDb(devId, pm1Measurements, pm25Measurements)
            } catch (e: Exception) {
                Log.e(TAG, "V2: saveSyncChunkToDb EXCEPTION", e)
            }
        }
    }

    private suspend fun saveLiveMeasurementToDb(
        devId: String,
        pm1Measurements: List<Measurement>,
        pm25Measurements: List<Measurement>,
    ) {
        val sessionId = sessionsRepository.getMobileActiveSessionIdByDeviceId(devId) ?: run {
            Log.e(TAG, "V2: No active mobile session for live measurement, deviceId=$devId")
            return
        }
        saveMeasurementsToSession(sessionId, devId, pm1Measurements, pm25Measurements)
        postLiveMeasurementEvents(devId, pm1Measurements, pm25Measurements)
    }

    private fun postLiveMeasurementEvents(
        devId: String,
        pm1Measurements: List<Measurement>,
        pm25Measurements: List<Measurement>,
    ) {
        val packageName = "AirBeamMini:$devId"
        val bus = EventBus.getDefault()
        pm1Measurements.forEach { m ->
            bus.post(
                NewMeasurementEvent(
                    packageName,
                    "AirBeamMini-PM1",
                    "Particulate Matter",
                    "PM",
                    "microgram per cubic meter",
                    "µg/m³",
                    0, 9, 35, 55, 150,
                    m.value,
                )
            )
        }
        pm25Measurements.forEach { m ->
            bus.post(
                NewMeasurementEvent(
                    packageName,
                    "AirBeamMini-PM2.5",
                    "Particulate Matter",
                    "PM",
                    "microgram per cubic meter",
                    "µg/m³",
                    0, 9, 35, 55, 150,
                    m.value,
                )
            )
        }
    }

    private suspend fun saveSyncChunkToDb(
        devId: String,
        pm1Measurements: List<Measurement>,
        pm25Measurements: List<Measurement>,
    ) {
        Log.d(TAG, "V2: saveSyncChunkToDb called, devId=$devId, pm1Count=${pm1Measurements.size}")

        val recordingId = sessionsRepository.getMobileActiveSessionIdByDeviceId(devId)
        val disconnectedId = sessionsRepository.getMobileDisconnectedSessionIdByDeviceId(devId)
        Log.d(TAG, "V2: Session lookup: recordingId=$recordingId, disconnectedId=$disconnectedId")

        val sessionId = recordingId ?: disconnectedId ?: run {
            Log.e(TAG, "V2: No mobile session found for deviceId=$devId, cannot save sync measurements")
            return
        }

        // Discard sync data that belongs to a different (older) session on the device.
        val deviceUuid = savedSessionUuid?.let { leBytesToUuid(it) }
        if (deviceUuid != null) {
            val appSession = sessionsRepository.getSessionById(sessionId)
            if (appSession?.uuid != deviceUuid) {
                Log.w(TAG, "V2: Discarding sync data from device session $deviceUuid (app session: ${appSession?.uuid})")
                return
            }
        }

        saveMeasurementsToSession(sessionId, devId, pm1Measurements, pm25Measurements)
        Log.d(TAG, "V2: Saved ${pm1Measurements.size} synced measurements to DB (sessionId=$sessionId, deviceId=$devId)")
    }

    private suspend fun saveMeasurementsToSession(
        sessionId: Long,
        devId: String,
        pm1Measurements: List<Measurement>,
        pm25Measurements: List<Measurement>,
    ) {
        val packageName = "AirBeamMini:$devId"

        val pm1Stream = MeasurementStream(
            sensorPackageName = packageName,
            sensorName = "AirBeamMini-PM1",
            measurementType = "Particulate Matter",
            measurementShortType = "PM",
            unitName = "microgram per cubic meter",
            unitSymbol = "µg/m³",
            thresholdVeryLow = 0,
            thresholdLow = 9,
            thresholdMedium = 35,
            thresholdHigh = 55,
            thresholdVeryHigh = 150,
        )
        val pm1StreamId = measurementStreamsRepository.getIdOrInsert(sessionId, pm1Stream)
        measurementsRepository.insertAll(pm1StreamId, sessionId, pm1Measurements)
        activeSessionMeasurementsRepository.createOrReplaceMultipleRows(pm1StreamId, sessionId, pm1Measurements)

        val pm25Stream = MeasurementStream(
            sensorPackageName = packageName,
            sensorName = "AirBeamMini-PM2.5",
            measurementType = "Particulate Matter",
            measurementShortType = "PM",
            unitName = "microgram per cubic meter",
            unitSymbol = "µg/m³",
            thresholdVeryLow = 0,
            thresholdLow = 9,
            thresholdMedium = 35,
            thresholdHigh = 55,
            thresholdVeryHigh = 150,
        )
        val pm25StreamId = measurementStreamsRepository.getIdOrInsert(sessionId, pm25Stream)
        measurementsRepository.insertAll(pm25StreamId, sessionId, pm25Measurements)
        activeSessionMeasurementsRepository.createOrReplaceMultipleRows(pm25StreamId, sessionId, pm25Measurements)
    }

    // -- UUID LE conversion --

    /**
     * Reverse of [uuidToLeBytes]: reconstruct a UUID string from 16-byte mixed-endian encoding.
     */
    private fun leBytesToUuid(bytes: ByteArray): String {
        require(bytes.size == 16) { "UUID must be 16 bytes, got ${bytes.size}" }

        // Reverse the first 3 groups back to big-endian
        val std = byteArrayOf(
            bytes[3], bytes[2], bytes[1], bytes[0],   // time_low
            bytes[5], bytes[4],                         // time_mid
            bytes[7], bytes[6],                         // time_hi_and_version
            bytes[8], bytes[9], bytes[10], bytes[11],   // clock_seq + node
            bytes[12], bytes[13], bytes[14], bytes[15]
        )

        val buffer = ByteBuffer.wrap(std)
        val msb = buffer.long
        val lsb = buffer.long
        return UUID(msb, lsb).toString()
    }

    private fun logError(operation: String, status: Int) {
        Log.e(TAG, "V2: $operation failed with status $status")
    }
}
