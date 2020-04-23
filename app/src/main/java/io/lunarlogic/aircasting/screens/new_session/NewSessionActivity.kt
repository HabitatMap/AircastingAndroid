package io.lunarlogic.aircasting.screens.new_session

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.bluetooth.BluetoothActivity
import io.lunarlogic.aircasting.lib.ResultCodes
import io.lunarlogic.aircasting.permissions.PermissionsManager
import java.io.IOException
import java.util.*

class NewSessionActivity : AppCompatActivity(), BluetoothActivity {
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

    override fun requestBluetoothPermissions(permissionsManager: PermissionsManager) {
        permissionsManager.requestBluetoothPermissions(this)
    }

    override fun bluetoothPermissionsGranted(permissionsManager: PermissionsManager): Boolean {
        return permissionsManager.bluetoothPermissionsGranted(this)
    }

    override fun requestBluetoothEnable() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(intent, ResultCodes.AIRCASTING_REQUEST_BLUETOOTH_ENABLE)
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
        mNewSessionController!!.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mNewSessionController!!.onActivityResult(requestCode, resultCode, data)
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

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(SPP_SERIAL)
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
