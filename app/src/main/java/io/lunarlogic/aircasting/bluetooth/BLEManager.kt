package io.lunarlogic.aircasting.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.sensor.ResponseParser
import io.lunarlogic.aircasting.sensor.Session
import io.lunarlogic.aircasting.sensor.airbeam2.HexMessagesBuilder
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import org.greenrobot.eventbus.EventBus
import java.util.*

class BLEManager(context: Context, private val settings: Settings) : BleManager(context) {
    companion object {
        val TAG = "MyBleManager"
        val SERVICE_UUID = UUID.fromString("0000ffdd-0000-1000-8000-00805f9b34fb")
        val MAX_MTU = 517
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

    val responseParser = ResponseParser()
    val hexMessagesBuilder = HexMessagesBuilder()

    override fun getGattCallback(): BleManagerGattCallback {
        return MyManagerGattCallback()
    }

    override fun log(priority: Int, message: String) {
        Log.println(priority, TAG, message)
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

    private inner class MyManagerGattCallback : BleManagerGattCallback() {
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
                    onCharacteristicChanged(data)
                }

                queue.add(enableNotifications(characteristic))
            }
            queue.enqueue()
        }

        override fun onDeviceDisconnected() {
            readCharacteristics = null
            configurationCharacteristic = null
        }
    }

    // TODO: move it to a separate service?
    fun onCharacteristicChanged(data: Data) {
        val value = data.value ?: return

        val dataString = String(value)

        if (!dataString.isEmpty()) {
            val newMeasurementEvent = responseParser.parse(dataString)
            newMeasurementEvent?.let { EventBus.getDefault().post(newMeasurementEvent) }
        }
    }

    fun sendAuth(uuid: String) {
        configurationCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        beginAtomicRequestQueue()
            .add(writeCharacteristic(configurationCharacteristic, hexMessagesBuilder.uuidMessage(uuid)))
            .add(writeCharacteristic(configurationCharacteristic, hexMessagesBuilder.authTokenMessage(settings.getAuthToken()!!)))
            .enqueue()
    }

    fun configureMobile() {
        configurationCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        beginAtomicRequestQueue()
            .add(writeCharacteristic(configurationCharacteristic, hexMessagesBuilder.bluetoothConfigurationMessage))
            .add(requestMtu(MAX_MTU))
            .enqueue()
    }

    fun configureFixedWifi(location: Session.Location, wifiSSID: String, wifiPassword: String) {
        configurationCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        beginAtomicRequestQueue()
            .add(writeCharacteristic(configurationCharacteristic, hexMessagesBuilder.locationMessage(location.latitude, location.longitude)))
            .add(writeCharacteristic(configurationCharacteristic, hexMessagesBuilder.wifiConfigurationMessage(wifiSSID, wifiPassword)))
            .enqueue()
    }

    fun configureFixedCellular(location: Session.Location) {
        configurationCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        beginAtomicRequestQueue()
            .add(writeCharacteristic(configurationCharacteristic, hexMessagesBuilder.locationMessage(location.latitude, location.longitude)))
            .add(writeCharacteristic(configurationCharacteristic, hexMessagesBuilder.cellularConfigurationMessage))
            .enqueue()
    }
}
