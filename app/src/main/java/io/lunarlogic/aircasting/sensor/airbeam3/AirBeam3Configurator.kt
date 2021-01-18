package io.lunarlogic.aircasting.sensor.airbeam3

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import io.lunarlogic.aircasting.exceptions.AirBeam3ConfiguringFailed
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.DateConverter
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.sensor.HexMessagesBuilder
import io.lunarlogic.aircasting.sensor.SyncFileChecker
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.RequestQueue
import no.nordicsemi.android.ble.WriteRequest
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileWriter
import java.util.*

class AirBeam3Configurator(
    context: Context,
    private val mErrorHandler: ErrorHandler,
    private val mSettings: Settings
): BleManager(context) {
    companion object {
        val SERVICE_UUID = UUID.fromString("0000ffdd-0000-1000-8000-00805f9b34fb")
        val MAX_MTU = 517
        val DATE_FORMAT = "dd/MM/yy-HH:mm:ss"
    }

    // used for sending hex codes to the AirBeam
    private val CONFIGURATION_CHARACTERISTIC_UUID = UUID.fromString("0000ffde-0000-1000-8000-00805f9b34fb")

    // have notifications about new measurements
    private val MEASUREMENTS_CHARACTERISTIC_UUIDS = arrayListOf(
        UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"),    // Temperature
        UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb"),    // Humidity
        UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb"),    // PM1
        UUID.fromString("0000ffe5-0000-1000-8000-00805f9b34fb"),    // PM2.5
        UUID.fromString("0000ffe6-0000-1000-8000-00805f9b34fb")     // PM10
    )
    // has notifications about measurements count in particular csv file on SD card
    private val SYNC_COUNTER_CHARACTERISTIC_UUID = UUID.fromString("0000ffde-0000-1000-8000-00805f9b34fb")
    // has notifications for reading measurements stored in csv files on SD card
    private val SYNC_CHARACTERISTIC_UUID = UUID.fromString("0000ffdf-0000-1000-8000-00805f9b34fb")

    private var measurementsCharacteristics: List<BluetoothGattCharacteristic>? = null
    private var configurationCharacteristic: BluetoothGattCharacteristic? = null
    private var syncCharacteristic: BluetoothGattCharacteristic? = null
    private var syncCounterCharacteristic: BluetoothGattCharacteristic? = null
    private val SYNC_FINISH = "SD_SYNC_FINISH"
    private val SYNC_TAG = "SYNC"
    private val CLEAR_FINISH = "SD_DELETE_FINISH"

    private var syncFileWriter: FileWriter? = null
    private var count = 0
    private var counter = 0

    private var step = 0 // TOOD: remove it after implementing proper sync
    private var bleCount = 0 // TOOD: remove it after implementing proper sync
    private var wifiCount = 0 // TOOD: remove it after implementing proper sync
    private var cellularCount = 0 // TOOD: remove it after implementing proper sync
    class SyncEvent(val message: String) // TOOD: remove it after implementing proper sync

    val hexMessagesBuilder = HexMessagesBuilder()
    val airBeam3Reader = AirBeam3Reader(mErrorHandler)

    fun sendAuth(uuid: String) {
        configurationCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        beginAtomicRequestQueue()
            .add(uuidRequest(uuid))
            .add(sleep(500))
            .add(authRequest())
            .enqueue()
    }

    fun configure(
        session: Session,
        wifiSSID: String?,
        wifiPassword: String?
    ){
        val location = session.location ?: return
        val dateString = DateConverter.toDateString(Date(), TimeZone.getDefault(), DATE_FORMAT)

        if (session.isFixed()) {
            when (session.streamingMethod) {
                Session.StreamingMethod.WIFI -> configureFixedWifi(location, dateString, wifiSSID, wifiPassword)
                Session.StreamingMethod.CELLULAR -> configureFixedCellular(location, dateString)
            }
        } else {
            configureMobileSession(location, dateString)
        }
    }

    fun reconnectMobileSession() {
        configurationCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        beginAtomicRequestQueue()
            .add(requestMtu(MAX_MTU))
            .add(sleep(500))
            .add(mobileModeRequest())
            .enqueue()
    }

    fun sync() {
        configurationCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        beginAtomicRequestQueue()
            .add(requestMtu(MAX_MTU))
            .add(sleep(500))
            .add(syncModeRequest())
            .enqueue()
    }

    fun clearSDCard() {
        configurationCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        beginAtomicRequestQueue()
            .add(clearSDCardModeRequest())
            .add(sleep(500))
            .enqueue()
    }

    private fun configureMobileSession(location: Session.Location, dateString: String) {
        configurationCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        beginAtomicRequestQueue()
            .add(sendLocationConfiguration(location))
            .add(sleep(500))
            .add(sendCurrentTimeConfiguration(dateString))
            .add(sleep(500))
            .add(requestMtu(MAX_MTU))
            .add(sleep(500))
            .add(mobileModeRequest())
            .enqueue()
    }

    private fun configureFixedWifi(location: Session.Location, dateString: String, wifiSSID: String?, wifiPassword: String?) {
        configurationCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        wifiSSID ?: return
        wifiPassword ?: return

        beginAtomicRequestQueue()
            .add(sendLocationConfiguration(location))
            .add(sleep(500))
            .add(sendCurrentTimeConfiguration(dateString))
            .add(sleep(500))
            .add(sendWifiConfiguration(wifiSSID, wifiPassword))
            .enqueue()
    }

    private fun configureFixedCellular(location: Session.Location, dateString: String) {
        configurationCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        beginAtomicRequestQueue()
            .add(sendLocationConfiguration(location))
            .add(sleep(500))
            .add(sendCurrentTimeConfiguration(dateString))
            .add(sleep(500))
            .add(cellularModeRequest())
            .add(sleep(1000))
            .enqueue()
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return GattCallback()
    }

    private fun validateReadCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {
        characteristic ?: return false

        val properties: Int = characteristic.properties
        return properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
    }

    private fun validateConfigurationCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {
        characteristic ?: return false

        val properties: Int = characteristic.properties
        return properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0
    }

    private inner class GattCallback : BleManagerGattCallback() {
        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(SERVICE_UUID)

            service ?: return false

            measurementsCharacteristics = MEASUREMENTS_CHARACTERISTIC_UUIDS.mapNotNull { uuid -> service.getCharacteristic(uuid) }

            measurementsCharacteristics?.isEmpty() ?: return false

            configurationCharacteristic = service.getCharacteristic(CONFIGURATION_CHARACTERISTIC_UUID)
            syncCharacteristic = service.getCharacteristic(SYNC_CHARACTERISTIC_UUID)
            syncCounterCharacteristic = service.getCharacteristic(SYNC_COUNTER_CHARACTERISTIC_UUID)

            val characteristics = measurementsCharacteristics!!.union(arrayListOf(syncCharacteristic, syncCounterCharacteristic))
            if (characteristics.any { characteristic -> !validateReadCharacteristic(characteristic) }) {
                return false
            }

            if (!validateConfigurationCharacteristic(configurationCharacteristic)) {
                return false
            }

            return true
        }

        override fun initialize() {
            enableNotifications()
        }

        private fun enableSyncNotifications(queue: RequestQueue) {
            val syncCharacteristic = syncCharacteristic ?: return
            val syncCounterCharacteristic = syncCounterCharacteristic ?: return

            setupSyncCounterCallback()
            setupSyncCallback()

            arrayListOf(syncCounterCharacteristic, syncCharacteristic).forEach { characteristic ->
                queue.add(
                    enableNotifications(characteristic)
                        .fail { _, status -> onNotificationEnableFailure(characteristic, status) }
                ).add(sleep(500))
            }
        }

        private fun setupSyncCounterCallback() {
            count = 0

            val callback = setNotificationCallback(syncCounterCharacteristic)
            callback.with { _, data ->
                val value = data.value
                value?.let {
                    val valueString = String(value)

                    try {
                        val partialCountString = valueString.split(":").lastOrNull()?.trim()
                        val partialCount = partialCountString?.toInt()

                        partialCount?.let {
                            step += 1
                            when(step) {
                                1 -> bleCount = partialCount
                                2 -> wifiCount = partialCount
                                3 -> cellularCount = partialCount
                            }
                            count += partialCount
                        }
                    } catch (e: NumberFormatException) {
                        // ignore - this is e.g. SD_SYNC_FINISH
                    }

                    if (valueString == SYNC_FINISH) {
                        Log.d(SYNC_TAG, "Sync finished")
                        closeSyncFile()
                        val syncMessage = "Synced $counter/$count."
                        showMessage("Syncing from SD card successfully finished.\n$syncMessage\nCheck files/sync/sync.txt")
                        checkOutputFile()
                    } else if (valueString == CLEAR_FINISH) {
                        showMessage("SD card successfully cleared.")
                    }
                }
            }
        }

        private fun setupSyncCallback() {
            counter = 0
            openSyncFile()

            val callback = setNotificationCallback(syncCharacteristic)
            callback.with { _, data ->
                val value = data.value

                value?.let {
                    val line = String(value)
                    writeToSyncFile(line)
                    counter += 1

                    showMessage("Syncing $counter/$count...")
                }
            }
        }

        private fun enableNotifications() {
            val queue = beginAtomicRequestQueue()

            enableSyncNotifications(queue)

            measurementsCharacteristics?.forEach { characteristic ->
                val callback = setNotificationCallback(characteristic)
                callback.with { _, data ->
                    airBeam3Reader.run(data)
                }

                queue.add(
                    enableNotifications(characteristic)
                        .fail { _, status -> onNotificationEnableFailure(characteristic, status) }
                ).add(sleep(500))
            }
            queue.enqueue()
        }

        private fun onNotificationEnableFailure(characteristic: BluetoothGattCharacteristic, status: Int) {
            mErrorHandler.handle(AirBeam3ConfiguringFailed("notification " + characteristic.uuid, status))
        }

        override fun onDeviceDisconnected() {
            measurementsCharacteristics = null
            configurationCharacteristic = null
            syncCharacteristic = null
            syncCounterCharacteristic = null
        }
    }

    private fun showMessage(message: String) {
        EventBus.getDefault().post(SyncEvent(message))
    }

    // TODO: this is temporary thing - remove this after implementing real sync
    private fun openSyncFile() {
        val dir = context.getExternalFilesDir("sync")

        val file = File(dir, "sync.txt")
        syncFileWriter = FileWriter(file)
    }

    // TODO: this is temporary thing - remove this after implementing real sync
    private fun writeToSyncFile(line: String) {
        syncFileWriter?.write(line)
    }

    // TODO: this is temporary thing - remove this after implementing real sync
    private fun closeSyncFile() {
        syncFileWriter?.flush()
        syncFileWriter?.close()
    }

    // TODO: this is temporary thing - remove this after implementing real sync
    private fun checkOutputFile() {
        showMessage("Checking sync file...")
        if (SyncFileChecker(context).run(bleCount, wifiCount, cellularCount)) {
            showMessage("Sync file is correct!")
        } else {
            showMessage("Something is wrong with the sync file :/")
        }
    }

    private fun uuidRequest(uuid: String): WriteRequest {
        return writeCharacteristic(configurationCharacteristic, hexMessagesBuilder.uuidMessage(uuid))
            .fail { _, status -> mErrorHandler.handle(AirBeam3ConfiguringFailed("uuid", status)) }
    }

    private fun authRequest(): WriteRequest {
        return writeCharacteristic(configurationCharacteristic, hexMessagesBuilder.authTokenMessage(mSettings.getAuthToken()!!))
            .fail { _, status -> mErrorHandler.handle(AirBeam3ConfiguringFailed("token", status)) }
    }

    private fun mobileModeRequest(): WriteRequest {
        return writeCharacteristic(configurationCharacteristic, hexMessagesBuilder.bluetoothConfigurationMessage)
            .fail { _, status -> mErrorHandler.handle(AirBeam3ConfiguringFailed("mobile mode", status)) }
    }

    private fun sendCurrentTimeConfiguration(dateString: String): WriteRequest {
        return writeCharacteristic(configurationCharacteristic, hexMessagesBuilder.currentTimeMessage(dateString))
            .fail { _, status -> mErrorHandler.handle(AirBeam3ConfiguringFailed("current time", status)) }
    }

    private fun sendLocationConfiguration(location: Session.Location): WriteRequest {
        return writeCharacteristic(configurationCharacteristic, hexMessagesBuilder.locationMessage(location.latitude, location.longitude))
            .fail { _, status -> mErrorHandler.handle(AirBeam3ConfiguringFailed("location", status)) }
    }

    private fun sendWifiConfiguration(wifiSSID: String, wifiPassword: String): WriteRequest {
        return writeCharacteristic(configurationCharacteristic, hexMessagesBuilder.wifiConfigurationMessage(wifiSSID, wifiPassword))
            .fail { _, status -> mErrorHandler.handle(AirBeam3ConfiguringFailed("wifi credentials", status)) }
    }

    private fun cellularModeRequest(): WriteRequest {
        return writeCharacteristic(configurationCharacteristic, hexMessagesBuilder.cellularConfigurationMessage)
            .fail { _, status -> mErrorHandler.handle(AirBeam3ConfiguringFailed("cellular mode", status)) }
    }

    private fun syncModeRequest(): WriteRequest {
        return writeCharacteristic(configurationCharacteristic, hexMessagesBuilder.syncConfigurationMessage)
            .fail { _, status -> mErrorHandler.handle(AirBeam3ConfiguringFailed("sync mode", status)) }
    }

    private fun clearSDCardModeRequest(): WriteRequest {
        return writeCharacteristic(configurationCharacteristic, hexMessagesBuilder.clearSDCardConfigurationMessage)
            .fail { _, status -> mErrorHandler.handle(AirBeam3ConfiguringFailed("clear SD card mode", status)) }
    }
}
