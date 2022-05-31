package pl.llp.aircasting.ui.view.screens.main

import android.content.IntentFilter
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.MobileNavigationDirections
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.api.services.ConnectivityManager
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.common.BatteryAlertDialog
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.ui.view.screens.login.LoginActivity
import pl.llp.aircasting.ui.view.screens.new_session.NewSessionActivity
import pl.llp.aircasting.ui.view.screens.onboarding.OnboardingActivity
import pl.llp.aircasting.ui.view.screens.sync.SyncActivity
import pl.llp.aircasting.util.ResultCodes
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.DisconnectExternalSensorsEvent
import pl.llp.aircasting.util.events.KeepScreenOnToggledEvent
import pl.llp.aircasting.util.events.LocationPermissionsResultEvent
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.SessionManager
import pl.llp.aircasting.util.safeRegister

class MainController(
    private val rootActivity: AppCompatActivity,
    private val mViewMvc: MainViewMvc,
    private val mSettings: Settings,
    private val mFragmentManager: FragmentManager,
    private val mApiServiceFactory: ApiServiceFactory
) {
    private var mSessionManager: SessionManager? = null
    private var mConnectivityManager: ConnectivityManager? = null
    private val mErrorHandler = ErrorHandler(rootActivity)

    fun onCreate() {
        if (!mSettings.onboardingDisplayed() && mSettings.getAuthToken() == null) {
            showOnboardingScreen()
        } else if (mSettings.getAuthToken() == null) {
            showLoginScreen()
        } else {
            setupDashboard()
        }

        NewSessionActivity.register(rootActivity, LocalSession.Type.FIXED)
        NewSessionActivity.register(rootActivity, LocalSession.Type.MOBILE)
        SyncActivity.register(rootActivity, onFinish = { goToDormantTab() })

        mSessionManager?.onStart()
    }

    fun onResume() {
        EventBus.getDefault().safeRegister(this)
    }

    fun onDestroy() {
        EventBus.getDefault().unregister(this)
        unregisterConnectivityManager()
        mSessionManager?.onStop()
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
    }

    private fun showLoginScreen() {
        LoginActivity.start(rootActivity)
        rootActivity.finish()
    }

    private fun showOnboardingScreen() {
        OnboardingActivity.start(rootActivity)
        rootActivity.finish()
    }

    private fun setupDashboard() {
        mErrorHandler.registerUser(mSettings.getEmail())

        val apiService = mApiServiceFactory.get(mSettings.getAuthToken()!!)
        mSessionManager = SessionManager(rootActivity, apiService, mSettings)

        mConnectivityManager = ConnectivityManager(apiService, rootActivity, mSettings)
        registerConnectivityManager()

        sync(apiService)
    }

    private fun sync(apiService: ApiService) {
        val mMobileSessionsSyncService =
            SessionsSyncService.get(apiService, mErrorHandler, mSettings)

        mMobileSessionsSyncService.sync(
            onStartCallback = { mViewMvc.showLoader() },
            finallyCallback = { mViewMvc.hideLoader() }
        )
    }

    private fun goToDormantTab() {
        val action =
            MobileNavigationDirections.actionGlobalDashboard(SessionsTab.MOBILE_DORMANT.value)
        Navigation.findNavController(rootActivity, R.id.nav_host_fragment)
            .navigate(action)
    }

    fun showBatteryOptimizationHelperDialog() {
        BatteryAlertDialog(
            mFragmentManager,
            rootActivity.getString(R.string.running_background),
            rootActivity.getString(R.string.battery_desc)
        ).show()
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_LOCATION -> {
                EventBus.getDefault().post(LocationPermissionsResultEvent(grantResults))
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun registerConnectivityManager() {
        val filter = IntentFilter(ConnectivityManager.ACTION)
        mConnectivityManager?.let { rootActivity.registerReceiver(it, filter) }
    }

    private fun unregisterConnectivityManager() {
        mConnectivityManager?.let { rootActivity.unregisterReceiver(it) }
    }

    @Subscribe
    fun onMessageEvent(event: KeepScreenOnToggledEvent) {
        if (mSettings.isKeepScreenOnEnabled()) rootActivity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else rootActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
