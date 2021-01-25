package io.lunarlogic.aircasting.screens.new_session

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.lib.AppBar
import io.lunarlogic.aircasting.lib.NavigationController
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.permissions.PermissionsManager
import io.lunarlogic.aircasting.screens.dashboard.DashboardPagerAdapter
import io.lunarlogic.aircasting.sensor.AirBeamConnectorFactory
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionBuilder
import javax.inject.Inject

class NewSessionActivity : AppCompatActivity() {

    private var controller: NewSessionController? = null

    @Inject
    lateinit var permissionsManager: PermissionsManager

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    @Inject
    lateinit var sessionBuilder: SessionBuilder

    companion object {
        val SESSION_TYPE_KEY = "sessionType"

        fun start(rootActivity: FragmentActivity?, sessionType: Session.Type) {
            rootActivity?.let{
                val startForResult = it.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    val tabId = DashboardPagerAdapter.tabIndexForSessionType(sessionType, Session.Status.RECORDING)
                    if (it.resultCode == RESULT_OK) {
                        NavigationController.goToDashboard(tabId)
                    }
                }

                val intent = Intent(it, NewSessionActivity::class.java)
                intent.putExtra(SESSION_TYPE_KEY, sessionType)
                startForResult.launch(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionType = intent.extras?.get(SESSION_TYPE_KEY) as Session.Type

        val app = application as AircastingApplication
        val appComponent = app.appComponent
        appComponent.inject(this)

        val view = NewSessionViewMvcImpl(layoutInflater, null)
        controller = NewSessionController(
            this,
            view,
            supportFragmentManager,
            permissionsManager,
            bluetoothManager,
            sessionBuilder,
            settings,
            sessionType
        )
        controller?.onCreate()

        setContentView(view.rootView)
        AppBar.setup(view.rootView, this)
    }

    override fun onStop() {
        super.onStop()
        controller?.onStop()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        controller?.onBackPressed()
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
