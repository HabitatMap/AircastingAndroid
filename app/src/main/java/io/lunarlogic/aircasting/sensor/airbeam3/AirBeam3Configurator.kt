package io.lunarlogic.aircasting.sensor.airbeam3

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import io.lunarlogic.aircasting.exceptions.AirBeam3ConfiguringFailed
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.DateConverter
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.sensor.HexMessagesBuilder
import io.lunarlogic.aircasting.models.Session
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.WriteRequest
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

    private val CONFIGURATION_CHARACTERISTIC_UUID = UUID.fromString("0000ffde-0000-1000-8000-00805f9b34fb")
    private val READ_CHARACTERISTIC_UUIDS = arrayListOf(
        UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"),    // Temperature
        UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb"),    // Humidity
        UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb"),    // PM1
        UUID.fromString("0000ffe5-0000-1000-8000-00805f9b34fb"),    // PM2.5
        UUID.fromString("0000ffe6-0000-1000-8000-00805f9b34fb")     // PM10
    )

    private var readCharacteristics: List<BluetoothGattCharacteristic>? = null
    private var configurationCharacteristic: BluetoothGattCharacteristic? = null

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
            .add(mobileModeRequest())
            .add(sleep(500))
            .add(requestMtu(MAX_MTU))
            .enqueue()
    }

    private fun configureMobileSession(location: Session.Location, dateString: String) {
        configurationCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        beginAtomicRequestQueue()
            .add(sendLocationConfiguration(location))
            .add(sleep(500))
            .add(sendCurrentTimeConfiguration(dateString))
            .add(sleep(500))
            .add(mobileModeRequest())
            .add(sleep(500))
            .add(requestMtu(MAX_MTU))
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

    private fun validateReadCharacteristic(characteristic: BluetoothGattCharacteristic): Boolean {
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

            readCharacteristics = READ_CHARACTERISTIC_UUIDS.mapNotNull { uuid -> service.getCharacteristic(uuid) }

            readCharacteristics?.isEmpty() ?: return false

            configurationCharacteristic = service.getCharacteristic(CONFIGURATION_CHARACTERISTIC_UUID)

            if (readCharacteristics!!.any { characteristic -> !validateReadCharacteristic(characteristic) }) {
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

        private fun enableNotifications() {
            val queue = beginAtomicRequestQueue()
            readCharacteristics?.forEach { characteristic ->
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
            readCharacteristics = null
            configurationCharacteristic = null
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
}
