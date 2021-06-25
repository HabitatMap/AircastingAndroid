package pl.llp.aircasting.screens.sync

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.bluetooth.BluetoothManager
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.lib.AppBar
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.networking.services.ApiServiceFactory
import pl.llp.aircasting.permissions.PermissionsManager
import javax.inject.Inject

class SyncActivity: AppCompatActivity() {
    private var controller: SyncController? = null

    @Inject
    lateinit var permissionsManager: PermissionsManager

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    @Inject
    lateinit var errorHandler: ErrorHandler

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    companion object {
        fun start(rootActivity: FragmentActivity?, onFinish: (() -> Unit)? = null) {
            rootActivity ?: return

            val intent = Intent(rootActivity, SyncActivity::class.java)

            if (onFinish != null) {
                val startForResult =
                    rootActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                        onFinish.invoke()
                    }
                startForResult.launch(intent)
            } else {
                rootActivity.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as AircastingApplication
        val appComponent = app.appComponent
        appComponent.inject(this)

        val view = SyncViewMvcImpl(layoutInflater, null)
        controller = SyncController(
            this,
            view,
            permissionsManager,
            bluetoothManager,
            supportFragmentManager,
            apiServiceFactory,
            errorHandler,
            settings
        )

        controller?.onCreate()

        setContentView(view.rootView)
        AppBar.setup(view.rootView, this)
    }

    override fun onStop() {
        super.onStop()
        controller?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppBar.destroy()
    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        controller?.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        controller?.onActivityResult(requestCode, resultCode)
    }
}
