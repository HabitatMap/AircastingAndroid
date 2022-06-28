package pl.llp.aircasting.ui.view.screens.sync

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.setupAppBar
import javax.inject.Inject

class SyncActivity : BaseActivity() {
    private var controller: SyncController? = null

    @Inject
    lateinit var permissionsManager: PermissionsManager

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    @Inject
    lateinit var errorHandler: ErrorHandler

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    companion object {
        private var launcher: ActivityResultLauncher<Intent>? = null

        fun register(rootActivity: FragmentActivity?, onFinish: (() -> Unit)? = null) {
            rootActivity?.let {
                launcher =
                    it.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                        if (onFinish != null) {
                            onFinish.invoke()
                        }
                    }
            }
        }

        fun start(rootActivity: FragmentActivity?) {
            rootActivity ?: return

            val intent = Intent(rootActivity, SyncActivity::class.java)

            launcher?.launch(intent)
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
        setupAppBar(this, topAppBar)
    }

    override fun onStop() {
        super.onStop()
        controller?.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        controller?.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        controller?.onActivityResult(requestCode, resultCode)
    }
}
