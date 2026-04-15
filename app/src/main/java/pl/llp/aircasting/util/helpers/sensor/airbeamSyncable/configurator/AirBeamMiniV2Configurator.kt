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
import no.nordicsemi.android.ble.BleManager
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.events.NewMeasurementEvent
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Date
import java.util.UUID

class AirBeamMiniV2Configurator(
    applicationContext: Context,
    private val errorHandler: ErrorHandler,
    private val coroutineScope: CoroutineScope,
    private val batteryLevelFlow: MutableSharedFlow<Int>,
    private val sessionsRepository: SessionsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository,
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
        private const val OPCODE_NEW_SESSION: Byte = 0x13
        private const val OPCODE_SET_TIME: Byte = 0x15

        private const val SYNC_RECORD_SIZE = 8

        private const val STATE_IDLE: Int = 0x00
        private const val STATE_HAS_SAVED_SESSION: Int = 0x01
        private const val STATE_RUNNING: Int = 0x02

        private const val RESPONSE_ACK: Int = 0x20
        private const val RESPONSE_NACK: Int = 0x21
        private const val RESPONSE_READY: Int = 0x22

        private const val SET_TIME_INTERVAL_MS = 3_600_000L
    }

    enum class DeviceState {
        IDLE, HAS_SAVED_SESSION, RUNNING, UNKNOWN
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

    var deviceId: String? = null
    var currentState: DeviceState = DeviceState.UNKNOWN
        private set
    var currentBatteryLevel: Int = -1
        private set
    var savedSessionUuid: ByteArray? = null
        private set
    var hasSavedMeasurements: Boolean = false
        private set

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

        queue.enqueue()

        // Send initial SetTime after subscriptions settle
        sendSetTime()
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
        return connect(device)
    }

    override fun closeConnection() {
        close()
    }

    override fun sendAuth(uuid: String) {
        // V2 has no auth handshake — no-op
        Log.d(TAG, "V2: sendAuth called (no-op, V2 has no auth)")
    }

    override fun configure(session: Session, wifiSSID: String?, wifiPassword: String?) {
        if (deviceId == null) deviceId = session.deviceId

        val cmd = commandCharacteristic ?: run {
            Log.e(TAG, "V2: Command characteristic not available")
            return
        }

        val payload = buildMobileSessionPayload(session.uuid)

        commandState = CommandState.WAITING_ACK
        sessionReadyDeferred = CompletableDeferred()

        writeCharacteristic(cmd, payload, WRITE_TYPE_DEFAULT)
            .fail { _, status ->
                Log.e(TAG, "V2: NewSessionConfig write failed, status=$status")
                commandState = CommandState.IDLE
                sessionReadyDeferred?.complete(false)
            }
            .enqueue()

        Log.d(TAG, "V2: NewSessionConfig sent (${payload.size} bytes), uuid=${session.uuid}")
    }

    override fun reconnectMobileSession() {
        sendSetTime()

        when (currentState) {
            DeviceState.RUNNING -> {
                // Sync + live data already flowing automatically
                startHourlySetTime()
                Log.d(TAG, "V2: Device Running, sync + live measurements flowing")
            }

            DeviceState.HAS_SAVED_SESSION -> {
                // Must send ContinueSession to activate; device then streams sync + live
                sendContinueSession()
                Log.d(TAG, "V2: HasSavedSession, sending ContinueSession")
            }

            else -> {
                Log.w(TAG, "V2: Unexpected state on reconnect: $currentState")
            }
        }
    }

    override fun triggerSDCardDownload() {
        // V2 uses Sync characteristic, not SD card — later phase
        Log.d(TAG, "V2: triggerSDCardDownload called (no-op, V2 uses Sync)")
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
        statusCharacteristic = null
        commandCharacteristic = null
        responseCharacteristic = null
        measurementCharacteristic = null
        syncCharacteristic = null
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
        if (bytes.size < 2) return

        val state = bytes[0].toInt() and 0xFF
        val battery = bytes[1].toInt() and 0xFF
        currentBatteryLevel = battery

        coroutineScope.launch { batteryLevelFlow.emit(battery) }

        when (state) {
            STATE_IDLE -> {
                currentState = DeviceState.IDLE
                savedSessionUuid = null
                hasSavedMeasurements = false
                Log.d(TAG, "V2 Status: Idle, battery=$battery%")
            }

            STATE_HAS_SAVED_SESSION -> {
                currentState = DeviceState.HAS_SAVED_SESSION
                if (bytes.size >= 19) {
                    savedSessionUuid = bytes.copyOfRange(2, 18)
                    hasSavedMeasurements = bytes[18].toInt() != 0
                }
                Log.d(TAG, "V2 Status: HasSavedSession, battery=$battery%, hasMeasurements=$hasSavedMeasurements")
            }

            STATE_RUNNING -> {
                currentState = DeviceState.RUNNING
                if (bytes.size >= 18) {
                    savedSessionUuid = bytes.copyOfRange(2, 18)
                }
                hasSavedMeasurements = false
                Log.d(TAG, "V2 Status: Running, battery=$battery%")
            }

            else -> {
                currentState = DeviceState.UNKNOWN
                Log.w(TAG, "V2 Status: Unknown state 0x${state.toString(16)}, battery=$battery%")
            }
        }
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
                commandState = CommandState.IDLE
                sessionReadyDeferred?.complete(false)
            }

            RESPONSE_READY -> {
                if (commandState == CommandState.WAITING_READY) {
                    Log.d(TAG, "V2: Ready received, session is active")
                    commandState = CommandState.IDLE
                    sessionReadyDeferred?.complete(true)
                    onSessionReady()
                }
            }

            else -> Log.w(TAG, "V2: Unknown response 0x${"%02x".format(opcode)}")
        }
    }

    private fun onSessionReady() {
        startHourlySetTime()
    }

    // -- Session config payload --

    private fun buildMobileSessionPayload(sessionUuid: String): ByteArray {
        // Mobile: 0x13 + 16B_UUID + 2B_interval(u16) + 0x01 = 20 bytes
        val buffer = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(OPCODE_NEW_SESSION)
        buffer.put(uuidToLeBytes(sessionUuid))
        buffer.putShort(1) // interval_seconds = 1
        buffer.put(0x01)   // mobile mode
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

        val devId = deviceId ?: "unknown"
        val packageName = "AirBeamMini:$devId"

        EventBus.getDefault().post(
            NewMeasurementEvent(
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
                measuredValue = pm1.toDouble()
            )
        )

        EventBus.getDefault().post(
            NewMeasurementEvent(
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
                measuredValue = pm25.toDouble()
            )
        )
    }

    // -- ContinueSession --

    private fun sendContinueSession() {
        val cmd = commandCharacteristic ?: run {
            Log.e(TAG, "V2: Command characteristic not available for ContinueSession")
            return
        }

        commandState = CommandState.WAITING_ACK
        sessionReadyDeferred = CompletableDeferred()

        writeCharacteristic(cmd, byteArrayOf(OPCODE_CONTINUE_SESSION), WRITE_TYPE_DEFAULT)
            .fail { _, status ->
                Log.e(TAG, "V2: ContinueSession write failed, status=$status")
                commandState = CommandState.IDLE
                sessionReadyDeferred?.complete(false)
            }
            .enqueue()

        Log.d(TAG, "V2: ContinueSession sent")
    }

    // -- Sync chunk parsing & DB saving --

    private fun parseSyncChunk(bytes: ByteArray) {
        Log.d(TAG, "V2: Sync callback fired, ${bytes.size} bytes, deviceId=$deviceId")

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

        val devId = deviceId ?: run {
            Log.e(TAG, "V2: deviceId is null when sync chunk arrived, cannot save")
            return
        }
        val pm1Measurements = mutableListOf<Measurement>()
        val pm25Measurements = mutableListOf<Measurement>()

        for (i in 0 until count) {
            val timestamp = buffer.getInt().toLong() and 0xFFFFFFFFL
            val pm1 = buffer.getShort().toInt() and 0xFFFF
            val pm25 = buffer.getShort().toInt() and 0xFFFF
            val time = Date(timestamp * 1000)

            pm1Measurements.add(Measurement(pm1.toDouble(), time))
            pm25Measurements.add(Measurement(pm25.toDouble(), time))
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

    private suspend fun saveSyncChunkToDb(
        devId: String,
        pm1Measurements: List<Measurement>,
        pm25Measurements: List<Measurement>,
    ) {
        Log.d(TAG, "V2: saveSyncChunkToDb called, devId=$devId, pm1Count=${pm1Measurements.size}")

        val recordingId = sessionsRepository.getMobileActiveSessionIdByDeviceId(devId)
        val disconnectedId = sessionsRepository.getMobileDisconnectedSessionIdByDeviceId(devId)
        Log.d(TAG, "V2: Session lookup: recordingId=$recordingId, disconnectedId=$disconnectedId")

        val sessionId = recordingId ?: disconnectedId
        if (sessionId == null) {
            Log.e(TAG, "V2: No mobile session found for deviceId=$devId, cannot save sync measurements")
            return
        }

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
        Log.d(TAG, "V2: PM1 streamId=$pm1StreamId, inserting ${pm1Measurements.size} measurements")
        measurementsRepository.insertAll(pm1StreamId, sessionId, pm1Measurements)
        activeSessionMeasurementsRepository.createOrReplaceMultipleRows(pm1StreamId, sessionId, pm1Measurements)
        Log.d(TAG, "V2: PM1 measurements inserted successfully")

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
        Log.d(TAG, "V2: PM2.5 streamId=$pm25StreamId, inserting ${pm25Measurements.size} measurements")
        measurementsRepository.insertAll(pm25StreamId, sessionId, pm25Measurements)
        activeSessionMeasurementsRepository.createOrReplaceMultipleRows(pm25StreamId, sessionId, pm25Measurements)
        Log.d(TAG, "V2: PM2.5 measurements inserted successfully")

        Log.d(TAG, "V2: Saved ${pm1Measurements.size} synced measurements to DB (sessionId=$sessionId, deviceId=$devId)")
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
