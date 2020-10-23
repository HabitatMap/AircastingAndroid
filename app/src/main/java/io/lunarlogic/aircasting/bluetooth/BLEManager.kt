package io.lunarlogic.aircasting.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import io.lunarlogic.aircasting.sensor.ResponseParser
import io.lunarlogic.aircasting.sensor.airbeam2.HexMessagesBuilder
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import org.greenrobot.eventbus.EventBus
import java.util.*

class BLEManager(context: Context) : BleManager(context) {
    companion object {
        val TAG = "MyBleManager"
        val SERVICE_UUID = UUID.fromString("0000ffdd-0000-1000-8000-00805f9b34fb")
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

        // Initialize your device here. Often you need to enable notifications and set required
        // MTU or write some initial data. Do it here.
        override fun initialize() {
            val hexMessagesBuilder = HexMessagesBuilder()

            configurationCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

//            val callback = setNotificationCallback(readCharacteristic)
//            callback.with { _, data ->
//                log(Log.INFO, "ANIA On characteristic changed! " + data.value.toString())
//                onCharacteristicChanged(data)
//            }

            beginAtomicRequestQueue()
                .add(
                    writeCharacteristic(configurationCharacteristic, hexMessagesBuilder.bluetoothConfigurationMessage)
                        .done { device: BluetoothDevice? ->
                            log(Log.INFO, "Configuration message sent!")
                        }
                )
//                .add(
//                    enableNotifications(readCharacteristic)
//                        .done { device: BluetoothDevice? ->
//                            log(Log.INFO, "Notification enabled")
//                        }
//                        .fail { _, status ->
//                            log(Log.ERROR, "Notification enable failed $status")
//                        }
//                )
                .enqueue()
        }

        override fun onDeviceDisconnected() {
            readCharacteristics = null
            configurationCharacteristic = null
        }
    }

    // TODO: move it to a separate service?
//    fun onCharacteristicChanged(data: Data) {
//        val value = data.value ?: return
//
//        val dataString = String(value)
//
//        if (!dataString.isEmpty()) {
//            val newMeasurementEvent = responseParser.parse(dataString)
//            newMeasurementEvent?.let { EventBus.getDefault().post(newMeasurementEvent) }
//        }
//    }
    fun run() {
        readCharacteristics?.forEach { characteristic ->
            pollCharacteristic(characteristic)
        }
    }

    fun pollCharacteristic(characteristic: BluetoothGattCharacteristic) {
        readCharacteristic(characteristic)
            .done {
                val data = String(characteristic.value)

                if (!data.isEmpty()) {
                    log(Log.INFO, "READ finished " + data)
                    val newMeasurementEvent = responseParser.parse(data)
                    newMeasurementEvent?.let { EventBus.getDefault().post(newMeasurementEvent) }
                }

                pollCharacteristic(characteristic)
            }
            .enqueue()
    }
}
