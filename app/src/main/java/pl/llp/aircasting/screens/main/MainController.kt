package pl.llp.aircasting.screens.main

import android.content.IntentFilter
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.events.DisconnectExternalSensorsEvent
import pl.llp.aircasting.events.KeepScreenOnToggledEvent
import pl.llp.aircasting.events.LocationPermissionsResultEvent
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.lib.NavigationController
import pl.llp.aircasting.lib.ResultCodes
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.lib.safeRegister
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.networking.services.ApiService
import pl.llp.aircasting.networking.services.ApiServiceFactory
import pl.llp.aircasting.networking.services.ConnectivityManager
import pl.llp.aircasting.networking.services.SessionsSyncService
import pl.llp.aircasting.screens.dashboard.DashboardPagerAdapter
import pl.llp.aircasting.screens.login.LoginActivity
import pl.llp.aircasting.screens.new_session.NewSessionActivity
import pl.llp.aircasting.screens.onboarding.OnboardingActivity
import pl.llp.aircasting.screens.sync.SyncActivity
import pl.llp.aircasting.sensor.SessionManager

class MainController(
    private val rootActivity: AppCompatActivity,
    private val mViewMvc: MainViewMvc,
    private val mSettings: Settings,
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

        NewSessionActivity.register(rootActivity, Session.Type.FIXED)
        NewSessionActivity.register(rootActivity, Session.Type.MOBILE)
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
        val tabId = DashboardPagerAdapter.tabIndexForSessionType(
            Session.Type.MOBILE,
            Session.Status.FINISHED
        )
        NavigationController.goToDashboard(tabId)
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
