package io.lunarlogic.aircasting.screens.new_session

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.di.AppModule
import io.lunarlogic.aircasting.di.PermissionsModule
import io.lunarlogic.aircasting.di.SettingsModule
import io.lunarlogic.aircasting.permissions.PermissionsActivity
import io.lunarlogic.aircasting.lib.ResultCodes
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.permissions.PermissionsManager
import io.lunarlogic.aircasting.sensor.SessionBuilder
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Connector
import javax.inject.Inject

class NewSessionActivity : AppCompatActivity(),
    PermissionsActivity {

    private lateinit var controller: NewSessionController

    @Inject
    lateinit var permissionsManager: PermissionsManager

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    @Inject
    lateinit var airbeam2Connector: AirBeam2Connector

    @Inject
    lateinit var sessionBuilder: SessionBuilder

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

        val app = application as AircastingApplication
        app.permissionsModule.permissionsActivity = this
        val appComponent = app.appComponent
        appComponent.inject(this)

        val view = NewSessionViewMvcImpl(layoutInflater, null)
        controller = NewSessionController(
            this,
            this,
            view,
            supportFragmentManager,
            permissionsManager,
            bluetoothManager,
            airbeam2Connector,
            sessionBuilder
        )

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

    override fun requestAudioPermissions(permissionsManager: PermissionsManager) {
        permissionsManager.requestAudioPermissions(this)
    }

    override fun audioPermissionsGranted(permissionsManager: PermissionsManager): Boolean {
        return permissionsManager.audioPermissionsGranted(this)
    }

    override fun onStart() {
        super.onStart()
        controller.onStart()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        controller.onBackPressed()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        controller.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        controller.onActivityResult(requestCode, resultCode)
    }
}
