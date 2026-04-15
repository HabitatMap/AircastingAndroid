package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.BleManager
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.exceptions.ErrorHandler
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

class AirBeamMiniV2Configurator(
    applicationContext: Context,
    private val errorHandler: ErrorHandler,
    private val coroutineScope: CoroutineScope,
    private val batteryLevelFlow: MutableSharedFlow<Int>,
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

        private const val OPCODE_SET_TIME: Byte = 0x15

        private const val STATE_IDLE: Int = 0x00
        private const val STATE_HAS_SAVED_SESSION: Int = 0x01
        private const val STATE_RUNNING: Int = 0x02

        private const val SET_TIME_INTERVAL_MS = 3_600_000L
    }

    enum class DeviceState {
        IDLE, HAS_SAVED_SESSION, RUNNING, UNKNOWN
    }

    private var statusCharacteristic: BluetoothGattCharacteristic? = null
    private var commandCharacteristic: BluetoothGattCharacteristic? = null
    private var responseCharacteristic: BluetoothGattCharacteristic? = null
    private var measurementCharacteristic: BluetoothGattCharacteristic? = null
    private var syncCharacteristic: BluetoothGattCharacteristic? = null

    private var setTimeJob: Job? = null

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
            Log.d(TAG, "V2 Response: ${bytes.joinToString { "0x%02x".format(it) }}")
        }
        queue.add(
            enableNotifications(responseCharacteristic)
                .fail { _, status -> logError("response notification", status) }
        ).add(sleep(300))

        // Measurement indications
        setNotificationCallback(measurementCharacteristic).with { _, data ->
            val bytes = data.value ?: return@with
            Log.d(TAG, "V2 Measurement received: ${bytes.size} bytes")
        }
        queue.add(
            enableIndications(measurementCharacteristic)
                .fail { _, status -> logError("measurement indication", status) }
        ).add(sleep(300))

        // Sync indications
        setNotificationCallback(syncCharacteristic).with { _, data ->
            val bytes = data.value ?: return@with
            Log.d(TAG, "V2 Sync received: ${bytes.size} bytes")
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
        // Session configuration is Phase 2
        Log.d(TAG, "V2: configure called (stub for Phase 2)")
    }

    override fun reconnectMobileSession() {
        // Reconnection is Phase 3
        Log.d(TAG, "V2: reconnectMobileSession called (stub for Phase 3)")
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

    private fun logError(operation: String, status: Int) {
        Log.e(TAG, "V2: $operation failed with status $status")
    }
}
