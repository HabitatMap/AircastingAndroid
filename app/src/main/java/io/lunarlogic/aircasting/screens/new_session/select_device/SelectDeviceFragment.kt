package io.lunarlogic.aircasting.screens.new_session.select_device

import android.bluetooth.*
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.bluetooth.BLEManager
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.sensor.airbeam2.HexMessagesBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class SelectDeviceFragment() : Fragment(), ConnectionObserver {
    private var controller: SelectDeviceController? = null
    var listener: SelectDeviceViewMvc.Listener? = null
    var bluetoothManager: BluetoothManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            SelectDeviceViewMvcImpl(
                layoutInflater,
                null
            )
        if (bluetoothManager != null && listener != null) {
            controller =
                SelectDeviceController(
                    context,
                    view,
                    bluetoothManager!!,
                    listener!!
                )
        }

        return view.rootView
    }

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)

    private val STATE_DISCONNECTED = 0
    private val STATE_CONNECTED = 1

    private var connectionState = STATE_DISCONNECTED

    private val SERVICE_UUID = UUID.fromString("0000ffdd-0000-1000-8000-00805f9b34fb")

    private val WRITE_CHARACTERISTIC_UUID = UUID.fromString("0000ffde-0000-1000-8000-00805f9b34fb")
    private val READ_CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

//    0000ffe0-0000-1000-8000-00805f9b34fb C - nie dziala
//    0000ffe1-0000-1000-8000-00805f9b34fb F
//    0000ffe2-0000-1000-8000-00805f9b34fb K - nie dziala
//    0000ffe3-0000-1000-8000-00805f9b34fb Humidity
//    0000ffe4-0000-1000-8000-00805f9b34fb PM1
//    0000ffe5-0000-1000-8000-00805f9b34fb PM2.5
//    0000ffe6-0000-1000-8000-00805f9b34fb PM10

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            println("ANIA status: %x ($status)".format(status))
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectionState = STATE_CONNECTED

                    println("ANIA connected :)!")

                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectionState = STATE_DISCONNECTED
                    println("ANIA disconnected :(")
                }
            }
//            gatt.disconnect()
//            gatt.close()
        }

        fun isWritable(characteristic: BluetoothGattCharacteristic): Boolean {
            return (characteristic.getProperties() and BluetoothGattCharacteristic.PROPERTY_WRITE == 0
                    && (characteristic.getProperties()
                    and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == 0)
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Thread.sleep(100)

            gatt.services.forEach { s ->
                println("ANIA " + s.uuid + ": " + s.characteristics.size)
                s.characteristics.forEach { c ->
                    println(
                        "ANIA " + c.uuid + ": " + c.descriptors.size + " " + isWritable(c) + " " + c.writeType + " " + isNotifiable(
                            c
                        )
                    )
                }
            }

            val service = gatt.getService(SERVICE_UUID)

            Thread.sleep(100)
            println("ANIA characteristics count " + service.characteristics.count())
            Thread.sleep(100)
            val characteristic = service.getCharacteristic(WRITE_CHARACTERISTIC_UUID)
            println("ANIA characteristic value " + characteristic?.value)
            println("ANIA characteristic uuid " + characteristic?.uuid)
            println("ANIA characteristic isWritable " + isWritable(characteristic))
            println("ANIA characteristic writeType " + characteristic.writeType)
            Thread.sleep(100)

            val hexMessagesBuilder = HexMessagesBuilder()
            characteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            characteristic?.setValue(hexMessagesBuilder.bluetoothConfigurationMessage)
            println("ANIA value " + characteristic.value)
            println("ANIA service " + characteristic.service)
            println("ANIA writeType " + characteristic.writeType)

            val status = gatt.writeCharacteristic(characteristic)
            println("ANIA status $status")
        }

        // Result of a characteristic read operation
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            println("ANIA onCharacteristicRead status $status")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    println("ANIA onCharacteristicRead success " + String(characteristic.value))

                    var status = gatt.readCharacteristic(characteristic)
                    println("ANIA readCharacteristic again $status")
                }
            }
        }

        fun isNotifiable(characteristic: BluetoothGattCharacteristic): Boolean {
            return (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            println("ANIA onCharacteristicWrite status $status")
            println("ANIA onCharacteristicWrite characteristic " + characteristic?.value)

            val service = gatt?.getService(SERVICE_UUID)
            val characteristic2 = service?.getCharacteristic(READ_CHARACTERISTIC_UUID)
            characteristic2 ?: return

            println("ANIA characteristic2 isNotifiable " + isNotifiable(characteristic2))

            var status = gatt.readCharacteristic(characteristic2)
            println("ANIA readCharacteristic $status")

//            println("ANIA characteristic2 value " + characteristic2.value)
//            println("ANIA characteristic2 descriptors " + characteristic2.descriptors.size)

//            val descriptorUUID = UUID.randomUUID()
//            val descriptor = BluetoothGattDescriptor(descriptorUUID, BluetoothGattDescriptor.PERMISSION_WRITE)
//            var status = characteristic2.addDescriptor(descriptor)
//            println("ANIA addDescriptor $status")
//            println("ANIA characteristic2 descriptors " + characteristic2.descriptors.size)


//            status = gatt.setCharacteristicNotification(characteristic2, true)
//            println("ANIA setCharacteristicNotification $status")
//
//            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//            println("ANIA descriptor " + descriptor.value)
//
//            status = gatt.writeDescriptor(descriptor)
//            println("ANIA writeDescriptor status $status")
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            println("ANIA onDescriptorWrite $status")

//            val service = gatt?.getService(SERVICE_UUID)
//            val characteristic = service?.getCharacteristic(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"))
//
//            var status = gatt?.readCharacteristic(characteristic)
//            println("ANIA readCharacteristic $status")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            println("ANIA onCharacteristicChanged value: " + characteristic?.value)
        }
    }

    private val connectionStarted = AtomicBoolean(false)

    override fun onStart() {
        super.onStart()

        activity?.packageManager?.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            println("ANIA ble not supported!")
        }

        val bluetoothManager: android.bluetooth.BluetoothManager = activity?.getSystemService(
            Context.BLUETOOTH_SERVICE
        ) as android.bluetooth.BluetoothManager;
        val bluetoothAdapter = bluetoothManager.adapter
        val leScanCallback2 =
            LeScanCallback { device, rssi, scanRecord ->

            }

        val manager = BLEManager(requireContext())
        manager.setConnectionObserver(this)

        val leScanCallback =
            LeScanCallback { device, rssi, scanRecord ->
                GlobalScope.launch(Dispatchers.IO) {
                    if (device.name == "AirBeam3:246f28c47698") {
                        bluetoothAdapter.stopLeScan(leScanCallback2)
                        if (connectionStarted.get() == false) {
                            println("ANIA found! trying to connect...")
                            connectionStarted.set(true)

                            manager.connect(device)
                                .timeout(100000)
                                .retry(3, 100)
                                .done({ device -> Log.i(BLEManager.TAG, "Device initiated") })
                                .enqueue()

//                            device.connectGatt(
//                                context,
//                                false,
//                                gattCallback,
//                                BluetoothDevice.TRANSPORT_LE
//                            )
                        }
                    }
                }
            }
        bluetoothAdapter.startLeScan(leScanCallback)

//        controller?.onStart()
    }

    override fun onStop() {
        super.onStop()
//        controller?.onStop()
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        Log.i(BLEManager.TAG, "onDeviceConnecting")
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        Log.i(BLEManager.TAG, "onDeviceConnected")
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        Log.i(BLEManager.TAG, "onDeviceFailedToConnect")
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        Log.i(BLEManager.TAG, "onDeviceReady")
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        Log.i(BLEManager.TAG, "onDeviceDisconnecting")
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        Log.i(BLEManager.TAG, "onDeviceDisconnected")
    }
}
