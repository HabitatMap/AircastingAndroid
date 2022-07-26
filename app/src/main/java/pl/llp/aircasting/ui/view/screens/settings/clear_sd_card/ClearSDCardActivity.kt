package pl.llp.aircasting.ui.view.screens.settings.clear_sd_card

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.extensions.setupAppBar
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.util.extensions.setupAppBar
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import javax.inject.Inject

class ClearSDCardActivity : BaseActivity() {
    private var controller: ClearSDCardController? = null

    @Inject
    lateinit var permissionsManager: PermissionsManager

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    companion object{
        fun start(rootActivity: FragmentActivity?) {
            rootActivity?.let{
                val intent = Intent(it, ClearSDCardActivity::class.java)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as AircastingApplication
        val appComponent = app.appComponent
        appComponent.inject(this)

        val view = ClearSDCardViewMvcImpl(layoutInflater, null)
        controller = ClearSDCardController(
            this,
            view,
            permissionsManager,
            bluetoothManager,
            supportFragmentManager,
            settings
        )

        controller?.onCreate()

        setContentView(view.rootView)
        setupAppBar(this, topAppBar)
    }

    override fun onResume() {
        super.onResume()
        controller?.onResume()
    }

    override fun onStop() {
        super.onStop()
        controller?.onStop()
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
