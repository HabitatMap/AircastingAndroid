package pl.llp.aircasting.screens.new_session

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.bluetooth.BluetoothManager
import pl.llp.aircasting.lib.AppBar
import pl.llp.aircasting.lib.NavigationController
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.models.SessionBuilder
import pl.llp.aircasting.permissions.PermissionsManager
import pl.llp.aircasting.screens.dashboard.DashboardPagerAdapter
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

    override fun onResume() {
        super.onResume()
        controller?.onResume()
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
