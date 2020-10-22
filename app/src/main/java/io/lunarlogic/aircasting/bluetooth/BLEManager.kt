package io.lunarlogic.aircasting.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import io.lunarlogic.aircasting.sensor.airbeam2.HexMessagesBuilder
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.PhyRequest
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback
import no.nordicsemi.android.ble.data.Data
import java.util.*


class BLEManager(context: Context) : BleManager(context) {
    companion object {
        val TAG = "MyBleManager"
        val SERVICE_UUID = UUID.fromString("0000ffdd-0000-1000-8000-00805f9b34fb")
    }

    private val WRITE_CHARACTERISTIC_UUID = UUID.fromString("0000ffde-0000-1000-8000-00805f9b34fb")
    private val READ_CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

    // Client characteristics
    private var readCharacteristic: BluetoothGattCharacteristic? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null

    override fun getGattCallback(): BleManagerGattCallback {
        return MyManagerGattCallback()
    }

    override fun log(priority: Int, message: String) {
        Log.println(priority, TAG, message)
    }

    /**
     * BluetoothGatt callbacks object.
     */
    private inner class MyManagerGattCallback : BleManagerGattCallback() {
        // This method will be called when the device is connected and services are discovered.
        // You need to obtain references to the characteristics and descriptors that you will use.
        // Return true if all required services are found, false otherwise.
        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(SERVICE_UUID)
            if (service != null) {
                readCharacteristic = service.getCharacteristic(READ_CHARACTERISTIC_UUID)
                writeCharacteristic = service.getCharacteristic(WRITE_CHARACTERISTIC_UUID)
            }
            // Validate properties
            var notify = false
            if (readCharacteristic != null) {
                val properties: Int = readCharacteristic!!.properties
                notify = properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
            }
            var writeRequest = false
            if (writeCharacteristic != null) {
                val properties: Int = writeCharacteristic!!.properties
                writeRequest = properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0
                writeCharacteristic!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            }
            // Return true if all required services have been found
            return readCharacteristic != null && writeCharacteristic != null && notify && writeRequest
        }

        // Initialize your device here. Often you need to enable notifications and set required
        // MTU or write some initial data. Do it here.
        override fun initialize() {
            val hexMessagesBuilder = HexMessagesBuilder()

//            writeCharacteristic(writeCharacteristic, hexMessagesBuilder.bluetoothConfigurationMessage)
//                .done { device: BluetoothDevice? ->
//                    log(Log.INFO, "Configuration message sent!")
//                }
//                .enqueue()

            // Set a callback for your notifications. You may also use waitForNotification(...).
            // Both callbacks will be called when notification is received.
            val callback = setNotificationCallback(readCharacteristic)
            callback.with { _, data ->
                log(Log.INFO, "On characteristic changed! " + data.value.toString())
            }

            // You may enqueue multiple operations. A queue ensures that all operations are
            // performed one after another, but it is not required.
            beginAtomicRequestQueue()
                .add(
                    writeCharacteristic(writeCharacteristic, hexMessagesBuilder.bluetoothConfigurationMessage)
                        .done { device: BluetoothDevice? ->
                            log(Log.INFO, "Configuration message sent!")
                        }
                )
//                .add(
//                    requestMtu(247)
//                        .with { device, mtu -> log(Log.INFO, "MTU set to " + mtu) }
//                        .fail { device, status -> log(Log.WARN, "Requested MTU not supported: " + status) }
//                )
//                .add(
//                    setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
//                        .fail { device, status -> log(Log.WARN, "Requested PHY not supported: " + status) }
//                )
                .add(
                    enableNotifications(readCharacteristic)
                        .done { device: BluetoothDevice? ->
                            log(Log.INFO, "Notification enabled")
                        }
                        .fail { _, status ->
                            log(Log.ERROR, "Notification enable failed $status")
                        }
                )
                .enqueue()
        }

        override fun onDeviceDisconnected() {
            // Device disconnected. Release your references here.
            readCharacteristic = null
            writeCharacteristic = null
        }
    }

    fun run() {
        readCharacteristic(readCharacteristic)
            .done {
                log(Log.INFO, "READ finished " + String(readCharacteristic!!.value))
                run()
            }
            .enqueue()
    }

    // Define your API.
    private abstract inner class FluxHandler : ProfileDataCallback {
        override fun onDataReceived(device: BluetoothDevice, data: Data) {
            log(Log.INFO, "onDataReceived " + data.toString())
        }
    }

    /**
     * Aborts time travel. Call during 3 sec after enabling Flux Capacitor and only if you don't
     * like 2020.
     */
    fun abort() {
        cancelQueue()
    }
}
