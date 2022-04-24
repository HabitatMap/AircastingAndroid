package pl.llp.aircasting.ui.view.screens.new_session

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.MobileNavigationDirections
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.SessionBuilder
import pl.llp.aircasting.ui.view.screens.common.BaseActivity
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.setupAppBar
import javax.inject.Inject

class NewSessionActivity : BaseActivity() {

    private var controller: NewSessionController? = null

    @Inject
    lateinit var permissionsManager: PermissionsManager

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    @Inject
    lateinit var sessionBuilder: SessionBuilder

    companion object {
        const val SESSION_TYPE_KEY = "sessionType"
        private var fixedLauncher: ActivityResultLauncher<Intent>? = null
        private var mobileLauncher: ActivityResultLauncher<Intent>? = null

        fun register(rootActivity: FragmentActivity?, sessionType: Session.Type) {
            rootActivity?.let {
                val launcher =
                    it.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                        if (it.resultCode == RESULT_OK) when (sessionType) {
                            Session.Type.FIXED -> goToFollowingTab(rootActivity)
                            Session.Type.MOBILE -> goToActiveTab(rootActivity)
                        }
                    }

                when (sessionType) {
                    Session.Type.FIXED -> fixedLauncher = launcher
                    Session.Type.MOBILE -> mobileLauncher = launcher
                }
            }
        }

        fun start(rootActivity: FragmentActivity?, sessionType: Session.Type) {
            rootActivity?.let {
                val intent = Intent(it, NewSessionActivity::class.java)
                intent.putExtra(SESSION_TYPE_KEY, sessionType)

                when (sessionType) {
                    Session.Type.FIXED -> fixedLauncher?.launch(intent)
                    Session.Type.MOBILE -> mobileLauncher?.launch(intent)
                }

            }
        }

        private fun goToActiveTab(rootActivity: FragmentActivity?) {
            val navHostFragment =
                rootActivity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val action = MobileNavigationDirections.actionGlobalDashboard(
                SessionsTab.MOBILE_ACTIVE.value
            )
            navHostFragment.navController.navigate(action)
        }

        private fun goToFollowingTab(rootActivity: FragmentActivity?) {
            val navHostFragment =
                rootActivity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val action = MobileNavigationDirections.actionGlobalDashboard(
                SessionsTab.FOLLOWING.value
            )
            navHostFragment.navController.navigate(action)
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

    override fun onBackPressed() {
        super.onBackPressed()
        controller?.onBackPressed()
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
