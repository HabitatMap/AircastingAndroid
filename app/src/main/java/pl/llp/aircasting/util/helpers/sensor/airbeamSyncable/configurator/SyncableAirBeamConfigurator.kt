package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.RequestQueue
import no.nordicsemi.android.ble.WriteRequest
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.AirBeam3ConfiguringFailed
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.location.LocationHelper
import pl.llp.aircasting.util.helpers.sensor.common.HexMessagesBuilder
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.reader.SyncableAirBeamReader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardReader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService.SDCardFileServiceProvider
import java.util.Date
import java.util.TimeZone
import java.util.UUID

open class SyncableAirBeamConfiguratorFactory(
    private val applicationContext: Context,
    private val mErrorHandler: ErrorHandler,
    private val mSettings: Settings,
    private val hexMessagesBuilder: HexMessagesBuilder,
    private val syncableAirBeamReader: SyncableAirBeamReader,
    private val sdCardFileServiceProvider: SDCardFileServiceProvider
) {
    private lateinit var sdCardReader: SDCardReader
    open fun create(type: DeviceItem.Type): SyncableAirBeamConfigurator {
        sdCardReader = SDCardReader(
            sdCardFileServiceProvider.get(type)
        )
        return when (type) {
            DeviceItem.Type.AIRBEAMMINI -> AirBeamMiniConfigurator(
                applicationContext,
                mErrorHandler,
                mSettings,
                hexMessagesBuilder,
                syncableAirBeamReader,
                sdCardReader
            )

            else -> AirBeam3Configurator(
                applicationContext,
                mErrorHandler,
                mSettings,
                hexMessagesBuilder,
                syncableAirBeamReader,
                sdCardReader
            )
        }
    }
}

abstract class SyncableAirBeamConfigurator(
    applicationContext: Context,
    private val mErrorHandler: ErrorHandler,
    private val mSettings: Settings,
    private val hexMessagesBuilder: HexMessagesBuilder,
    private val syncableAirBeamReader: SyncableAirBeamReader,
    private val sdCardReader: SDCardReader,
) : BleManager(applicationContext) {
    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("0000ffdd-0000-1000-8000-00805f9b34fb")
        const val MAX_MTU = 517
        const val DATE_FORMAT = "dd/MM/yy-HH:mm:ss"

        // used for sending hex codes to the AirBeam
        private val configurationCharacteristicUUID =
            UUID.fromString("0000ffde-0000-1000-8000-00805f9b34fb")

        // has notifications about measurements count in particular csv file on SD card
        private val downloadMetaDataFromSdCardCharacteristicUUID =
            UUID.fromString("0000ffde-0000-1000-8000-00805f9b34fb")

        // has notifications for reading measurements stored in csv files on SD card
        private val downloadFromSdCardCharacteristicUUID =
            UUID.fromString("0000ffdf-0000-1000-8000-00805f9b34fb")

        @JvmStatic
        protected val pm1SensorCharacteristic: UUID =
            UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb")

        @JvmStatic
        protected val pm2_5SensorCharacteristic: UUID =
            UUID.fromString("0000ffe5-0000-1000-8000-00805f9b34fb")
    }

    // have notifications about new measurements
    protected abstract val measurementsCharacteristicUUIDs: List<UUID>

    private var measurementsCharacteristics: List<BluetoothGattCharacteristic>? = null
    private var configurationCharacteristic: BluetoothGattCharacteristic? = null

    private var downloadFromSDCardCharacteristic: BluetoothGattCharacteristic? = null
    private var downloadMetaDataFromSDCardCharacteristic: BluetoothGattCharacteristic? = null

    fun sendAuth(uuid: String) {
        configurationCharacteristic?.writeType = WRITE_TYPE_DEFAULT

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
    ) {
        val location = session.sharableLocation() ?: return
        val dateString = DateConverter.toDateString(Date(), TimeZone.getDefault(), DATE_FORMAT)

        if (session.isFixed()) {
            when (session.streamingMethod) {
                Session.StreamingMethod.WIFI -> configureFixedWifi(
                    location,
                    dateString,
                    wifiSSID,
                    wifiPassword
                )

                Session.StreamingMethod.CELLULAR -> configureFixedCellular(location, dateString)
            }
        } else {
            configureMobileSession(location, dateString)
        }
    }

    fun reconnectMobileSession() {
        val location = Session.Location.get(LocationHelper.lastLocation())
        val dateString = DateConverter.toDateString(Date(), TimeZone.getDefault(), DATE_FORMAT)
        Log.d(
            TAG, "Reconnect mobile session with:\n" +
                    "Location: $location\n" +
                    "Time: $dateString"
        )
        configureMobileSession(location, dateString)
    }

    open fun triggerSDCardDownload() {
        configurationCharacteristic?.writeType = WRITE_TYPE_DEFAULT

        beginAtomicRequestQueue()
            .add(requestMtu(MAX_MTU))
            .add(sleep(500))
            .add(downloadFromSDCardModeRequest())
            .enqueue()
    }

    fun clearSDCard() {
        configurationCharacteristic?.writeType = WRITE_TYPE_DEFAULT

        beginAtomicRequestQueue()
            .add(clearSDCardModeRequest())
            .add(sleep(500))
            .enqueue()
    }

    private fun configureMobileSession(location: Session.Location, dateString: String) {
        configurationCharacteristic?.writeType = WRITE_TYPE_DEFAULT
        Log.d(
            TAG, "Configure mobile session with:\n" +
                    "Location: $location\n" +
                    "Time: $dateString"
        )
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

    private fun configureFixedWifi(
        location: Session.Location,
        dateString: String,
        wifiSSID: String?,
        wifiPassword: String?
    ) {
        configurationCharacteristic?.writeType = WRITE_TYPE_DEFAULT

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
        configurationCharacteristic?.writeType = WRITE_TYPE_DEFAULT

        beginAtomicRequestQueue()
            .add(sendLocationConfiguration(location))
            .add(sleep(500))
            .add(sendCurrentTimeConfiguration(dateString))
            .add(sleep(500))
            .add(cellularModeRequest())
            .add(sleep(1000))
            .enqueue()
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

    override fun log(priority: Int, message: String) {
        Log.println(priority, TAG, message)
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(SERVICE_UUID)

        service ?: return false

        measurementsCharacteristics =
            measurementsCharacteristicUUIDs.mapNotNull { uuid -> service.getCharacteristic(uuid) }

        measurementsCharacteristics?.isEmpty() ?: return false

        configurationCharacteristic = service.getCharacteristic(configurationCharacteristicUUID)
        downloadFromSDCardCharacteristic =
            service.getCharacteristic(downloadFromSdCardCharacteristicUUID)
        downloadMetaDataFromSDCardCharacteristic =
            service.getCharacteristic(downloadMetaDataFromSdCardCharacteristicUUID)

        val characteristics = measurementsCharacteristics!!.union(
            arrayListOf(
                downloadFromSDCardCharacteristic,
                downloadMetaDataFromSDCardCharacteristic
            )
        )
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

    private fun enableDownloadFromSDCardNotifications(queue: RequestQueue) {
        val downloadFromSDCardCharacteristic = downloadFromSDCardCharacteristic ?: return
        val downloadMetaDataFromSDCardCharacteristic =
            downloadMetaDataFromSDCardCharacteristic ?: return

        var metaDatacallback = setNotificationCallback(downloadMetaDataFromSDCardCharacteristic)
        metaDatacallback.with { _, data ->
            sdCardReader.onMetaDataDownloaded(data.value)
        }

        val measurementsCallback = setNotificationCallback(downloadFromSDCardCharacteristic)
        measurementsCallback.with { _, data ->
            sdCardReader.onMeasurementsDownloaded(data.value)
        }

        arrayListOf(
            downloadFromSDCardCharacteristic,
            downloadMetaDataFromSDCardCharacteristic
        ).forEach { characteristic ->
            queue.add(
                enableNotifications(characteristic)
                    .fail { _, status -> onNotificationEnableFailure(characteristic, status) }
            ).add(sleep(500))
        }
    }

    private fun enableNotifications() {
        val queue = beginAtomicRequestQueue()

        enableDownloadFromSDCardNotifications(queue)

        measurementsCharacteristics?.forEach { characteristic ->
            val callback = setNotificationCallback(characteristic)
            callback.with { _, data ->
                syncableAirBeamReader.run(data)
            }

            queue.add(
                enableNotifications(characteristic)
                    .fail { _, status -> onNotificationEnableFailure(characteristic, status) }
            ).add(sleep(500))
        }
        queue.enqueue()
    }

    private fun onNotificationEnableFailure(
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        mErrorHandler.handle(
            AirBeam3ConfiguringFailed(
                "notification " + characteristic.uuid,
                status
            )
        )
    }

    fun reset() {
        Log.d(TAG, "Resetting")
        measurementsCharacteristics = null
        configurationCharacteristic = null
        downloadFromSDCardCharacteristic = null
        downloadMetaDataFromSDCardCharacteristic = null
    }

    private fun uuidRequest(uuid: String): WriteRequest {
        return writeCharacteristic(
            configurationCharacteristic,
            hexMessagesBuilder.uuidMessage(uuid),
            WRITE_TYPE_DEFAULT
        )
            .fail { _, status -> mErrorHandler.handle(AirBeam3ConfiguringFailed("uuid", status)) }
    }

    private fun authRequest(): WriteRequest {
        return writeCharacteristic(
            configurationCharacteristic,
            hexMessagesBuilder.authTokenMessage(mSettings.getAuthToken()!!),
            WRITE_TYPE_DEFAULT
        )
            .fail { _, status -> mErrorHandler.handle(AirBeam3ConfiguringFailed("token", status)) }
    }

    private fun mobileModeRequest(): WriteRequest {
        return writeCharacteristic(
            configurationCharacteristic,
            hexMessagesBuilder.bluetoothConfigurationMessage,
            WRITE_TYPE_DEFAULT
        )
            .fail { _, status ->
                mErrorHandler.handle(
                    AirBeam3ConfiguringFailed(
                        "mobile mode",
                        status
                    )
                )
            }
    }

    private fun sendCurrentTimeConfiguration(dateString: String): WriteRequest {
        return writeCharacteristic(
            configurationCharacteristic,
            hexMessagesBuilder.currentTimeMessage(dateString),
            WRITE_TYPE_DEFAULT
        )
            .fail { _, status ->
                mErrorHandler.handle(
                    AirBeam3ConfiguringFailed(
                        "current time",
                        status
                    )
                )
            }
    }

    private fun sendLocationConfiguration(location: Session.Location): WriteRequest {
        return writeCharacteristic(
            configurationCharacteristic,
            hexMessagesBuilder.locationMessage(location.latitude, location.longitude),
            WRITE_TYPE_DEFAULT
        )
            .fail { _, status ->
                mErrorHandler.handle(
                    AirBeam3ConfiguringFailed(
                        "location",
                        status
                    )
                )
            }
    }

    private fun sendWifiConfiguration(wifiSSID: String, wifiPassword: String): WriteRequest {
        return writeCharacteristic(
            configurationCharacteristic,
            hexMessagesBuilder.wifiConfigurationMessage(wifiSSID, wifiPassword),
            WRITE_TYPE_DEFAULT
        )
            .fail { _, status ->
                mErrorHandler.handle(
                    AirBeam3ConfiguringFailed(
                        "wifi credentials",
                        status
                    )
                )
            }
    }

    private fun cellularModeRequest(): WriteRequest {
        return writeCharacteristic(
            configurationCharacteristic,
            hexMessagesBuilder.cellularConfigurationMessage,
            WRITE_TYPE_DEFAULT
        )
            .fail { _, status ->
                mErrorHandler.handle(
                    AirBeam3ConfiguringFailed(
                        "cellular mode",
                        status
                    )
                )
            }
    }

    private fun downloadFromSDCardModeRequest(): WriteRequest {
        return writeCharacteristic(
            configurationCharacteristic,
            hexMessagesBuilder.downloadFromSDCardConfigurationMessage,
            WRITE_TYPE_DEFAULT
        )
            .fail { _, status ->
                mErrorHandler.handle(
                    AirBeam3ConfiguringFailed(
                        "download from SD card mode",
                        status
                    )
                )
            }
    }

    private fun clearSDCardModeRequest(): WriteRequest {
        return writeCharacteristic(
            configurationCharacteristic,
            hexMessagesBuilder.clearSDCardConfigurationMessage,
            WRITE_TYPE_DEFAULT
        )
            .fail { _, status ->
                mErrorHandler.handle(
                    AirBeam3ConfiguringFailed(
                        "clear SD card mode",
                        status
                    )
                )
            }
    }
}
