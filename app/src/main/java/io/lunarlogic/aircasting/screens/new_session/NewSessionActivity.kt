package io.lunarlogic.aircasting.screens.new_session

import android.app.Activity
import android.app.Instrumentation
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.util.*

class NewSessionActivity : AppCompatActivity() {
    private var mNewSessionController: NewSessionController? = null
    var connectionStarted = false
    val SPP_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    companion object {
        fun start(context: Context?) {
            context?.let{
                val intent = Intent(it, NewSessionActivity::class.java)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val newSessionView = NewSessionViewMvcImpl(layoutInflater, null)
        mNewSessionController = NewSessionController(this, newSessionView, supportFragmentManager)

        setContentView(newSessionView.rootView)

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            println("Bluetooth intent receiver onReceive " + intent.action)
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    println("Bluetooth new device found name: " + device.name + " address: " + device.address)
                    if (device.name == "Airbeam2:0018961070D6") {
                        connectAirBeam2(device)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        mNewSessionController!!.onStart()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mNewSessionController!!.onBackPressed()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        println("Bluetooth onRequestPermissionsResult")
        when (requestCode) {
            AIRCASTING_PERMISSIONS_REQUEST_BLUETOOTH -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    println("Bluetooth permission enabled")
                    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    if (bluetoothAdapter == null) {
                        // TODO: handle that device does not support Bluetooth
                    }

                    if (bluetoothAdapter?.isEnabled == true) {
                        startDiscovery()
                    } else {
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(intent, AIRCASTING_REQUEST_BLUETOOTH_ENABLE)
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    println("Bluetooth permission denied :(")
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("Bluetooth onActivityResult")
        when (requestCode) {
            AIRCASTING_REQUEST_BLUETOOTH_ENABLE -> {
                if (resultCode == Activity.RESULT_OK) {
                    println("Bluetooth turn on! Yay!")

                    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
                    pairedDevices?.forEach { device ->
                        println("Bluetooth paired device name:" + device.name + " address: " + device.address)
                    }

                    startDiscovery()
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // TODO: handle
                    println("Bluetooth: user canceled turning on")
                }
            }

            else -> {
                // Ignore all other requests.
            }
        }
    }


    fun startDiscovery() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        adapter.startDiscovery()
    }

    fun connectAirBeam2(device: BluetoothDevice) {
        if (connectionStarted == false) {
            connectionStarted = true
            println("Bluetooth connectAirBeam2()")
            val thread = ConnectThread(device)
            thread.start()
        }
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private fun getSocketByHack(device: BluetoothDevice, channel: Int): BluetoothSocket? {
            try {
                val m = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                return m.invoke(device, channel) as BluetoothSocket
            } catch (e: NoSuchMethodException) {
                return null
            }

        }

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(SPP_SERIAL)
//            getSocketByHack(device, 1)
        }

        public override fun run() {
            println("Bluetooth connect thread started")
            // Cancel discovery because it otherwise slows down the connection.
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            bluetoothAdapter?.cancelDiscovery()

            mmSocket?.use { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                println("Bluetooth connecting...")
                socket.connect()


                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                // manageMyConnectedSocket(socket)
                println("Bluetooth CONNECTION SUCCEDED!")
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                // TODO: handle
            }
        }
    }
}
