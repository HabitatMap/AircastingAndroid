package io.lunarlogic.aircasting.screens.new_session

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Messenger
import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.bluetooth.BluetoothActivity
import io.lunarlogic.aircasting.lib.ResultCodes
import io.lunarlogic.aircasting.permissions.PermissionsManager

class NewSessionActivity : AppCompatActivity(), BluetoothActivity {
    private var controller: NewSessionController? = null

    companion object {
        fun start(context: Context?, messenger: Messenger) {
            context?.let{
                val intent = Intent(it, NewSessionActivity::class.java)
                intent.putExtra("messenger", messenger)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val messenger = intent.extras?.get("messenger") as Messenger

        val view = NewSessionViewMvcImpl(layoutInflater, null)
        controller = NewSessionController(this, this, view, supportFragmentManager, messenger)

        setContentView(view.rootView)
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

    override fun onStart() {
        super.onStart()
        controller!!.onStart()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        controller!!.onBackPressed()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        controller!!.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        controller!!.onActivityResult(requestCode, resultCode, data)
    }
}
