package io.lunarlogic.aircasting.screens.settings.clear_sd_card

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.lib.AppBar
import io.lunarlogic.aircasting.permissions.PermissionsManager
import javax.inject.Inject

class ClearSDCardActivity : AppCompatActivity() {
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

        val view = ClearSdCardViewMvcImpl(layoutInflater, null)
        controller = ClearSDCardController(
            this,
            view,
            permissionsManager,
            bluetoothManager,
            supportFragmentManager
        )

        controller?.onCreate()

        setContentView(view.rootView)
        AppBar.setup(view.rootView, this)
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
