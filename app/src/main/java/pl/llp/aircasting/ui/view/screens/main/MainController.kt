package pl.llp.aircasting.ui.view.screens.main

import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.services.ConnectivityManager
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.login.LoginActivity
import pl.llp.aircasting.ui.view.screens.new_session.NewSessionActivity
import pl.llp.aircasting.ui.view.screens.onboarding.OnboardingActivity
import pl.llp.aircasting.ui.view.screens.sync.SyncActivity
import pl.llp.aircasting.util.ResultCodes
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.*
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.goToMobileActiveTab
import pl.llp.aircasting.util.extensions.goToMobileDormantTab
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.helpers.sensor.SessionManager

@AssistedFactory
interface MainControllerFactory {
    fun create(
        rootActivity: AppCompatActivity,
    ): MainController
}

class MainController @AssistedInject constructor(
    @Assisted private val rootActivity: AppCompatActivity,
    private val mSettings: Settings,
    private val mSessionManager: SessionManager,
    private val mConnectivityManager: ConnectivityManager,
    private val mErrorHandler: ErrorHandler = ErrorHandler(rootActivity),
    private val sessionSyncService: SessionsSyncService,
) {

    fun onCreate() {
        val logout = EventBus.getDefault().getStickyEvent(LogoutEvent::class.java)

        if (!mSettings.onboardingDisplayed() && mSettings.getAuthToken() == null) {
            showOnboardingScreen()
        } else if (mSettings.getAuthToken() == null || logout?.inProgress == true) {
            showLoginScreen()
        } else {
            setupDashboard()
        }

        NewSessionActivity.register(rootActivity, Session.Type.FIXED)
        NewSessionActivity.register(rootActivity, Session.Type.MOBILE)
        SyncActivity.register(rootActivity, onFinish = { rootActivity.goToMobileDormantTab() })

        mSessionManager.onStart()
    }

    fun onResume() {
        EventBus.getDefault().safeRegister(this)
    }

    @Subscribe
    fun onMessageEvent(event: AppToForegroundEvent) = Handler(Looper.getMainLooper()).post {
        if (mSettings.mobileActiveSessionsCount() > 0)
            rootActivity.goToMobileActiveTab()
    }


    fun onDestroy() {
        EventBus.getDefault().unregister(this)
        unregisterConnectivityManager()
        mSessionManager.onStop()
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

        registerConnectivityManager()

        sync()
    }

    private fun sync() {
        rootActivity.lifecycleScope.launch {
            sessionSyncService.sync()
        }
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
        mConnectivityManager.let { rootActivity.registerReceiver(it, filter) }
    }

    private fun unregisterConnectivityManager() {
        mConnectivityManager.let { rootActivity.unregisterReceiver(it) }
    }

    @Subscribe
    fun onMessageEvent(event: KeepScreenOnToggledEvent) {
        if (mSettings.isKeepScreenOnEnabled()) rootActivity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else rootActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
