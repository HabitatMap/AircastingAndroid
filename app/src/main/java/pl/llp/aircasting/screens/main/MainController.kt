package pl.llp.aircasting.screens.main

import android.content.IntentFilter
import android.view.WindowManager
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.activity_main.*
import pl.llp.aircasting.events.DisconnectExternalSensorsEvent
import pl.llp.aircasting.events.LocationPermissionsResultEvent
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.lib.ResultCodes
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.networking.services.ApiService
import pl.llp.aircasting.networking.services.ApiServiceFactory
import pl.llp.aircasting.networking.services.ConnectivityManager
import pl.llp.aircasting.networking.services.SessionsSyncService
import pl.llp.aircasting.screens.new_session.LoginActivity
import pl.llp.aircasting.screens.onboarding.OnboardingActivity
import pl.llp.aircasting.sensor.SessionManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.events.KeepScreenOnToggledEvent
import pl.llp.aircasting.lib.safeRegister
import pl.llp.aircasting.R
import pl.llp.aircasting.lib.NavigationController
import pl.llp.aircasting.screens.dashboard.DashboardFragment

class MainController(
    private val rootActivity: AppCompatActivity,
    private val mViewMvc: MainViewMvcImpl, // TODO: changed from MainViewMvc, is it ok?!?!?
    private val mSettings: Settings,
    private val mApiServiceFactory: ApiServiceFactory,
    private val fragmentManager: FragmentManager
) : ReorderSessionsBottomSheet.Listener, MainViewMvc.Listener { //TODO: changed from MainViewMvc, is it ok????
    private var mSessionManager: SessionManager? = null
    private var mConnectivityManager: ConnectivityManager? = null
    private val mErrorHandler = ErrorHandler(rootActivity)
    private var mReorderSessionsBottomSheet: ReorderSessionsBottomSheet? = null

    fun onCreate() {
        if (!mSettings.onboardingDisplayed() && mSettings.getAuthToken() == null) {
            showOnboardingScreen()
        } else if (mSettings.getAuthToken() == null) {
            showLoginScreen()
        } else {
            setupDashboard()
        }

        mSessionManager?.onStart()
    }

    fun onResume() {
        EventBus.getDefault().safeRegister(this)
    }

    fun onStart(){
        mViewMvc.registerListener(this)
    }

    fun onStop(){
        mViewMvc.unregisterListener(this)
    }

    fun onDestroy() {
        EventBus.getDefault().unregister(this)
        rootActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        unregisterConnectivityManager()
        mSessionManager?.onStop()
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
    }

    fun registerListener(listener: MainViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: MainViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
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

        val apiService =  mApiServiceFactory.get(mSettings.getAuthToken()!!)
        mSessionManager = SessionManager(rootActivity, apiService, mSettings)

        mConnectivityManager = ConnectivityManager(apiService, rootActivity, mSettings)
        registerConnectivityManager()

        sync(apiService)
    }

    private fun sync(apiService: ApiService) {
        val mMobileSessionsSyncService = SessionsSyncService.get(apiService, mErrorHandler, mSettings)

        mMobileSessionsSyncService.sync(
            onStartCallback = { mViewMvc.showLoader() },
            finallyCallback = { mViewMvc.hideLoader() }
        )
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

    fun onMenuOpened() {
        mReorderSessionsBottomSheet = ReorderSessionsBottomSheet(this)
        mReorderSessionsBottomSheet?.show(fragmentManager)
    }

    override fun onReorderSessionsClicked() {
        //TODO("Not yet implemented") FOR SOME REASON THIS DRAWS NEW FRAGMENT ON TOP OF THE OLD ONE!!!!
        Log.i("SETT", "main " + mSettings.isReordering().toString())
        mSettings.setIsReordering(true)
        Log.i("SETT", "main " + mSettings.isReordering().toString())
        //fragmentManager.beginTransaction().replace(R.id.dashboard, DashboardFragment.newInstance()).commit()
//        fragmentManager.beginTransaction().detach(DashboardFragment()).attach(DashboardFragment()).commit()
        //TODO: mViewMvc.hideAppBarMenu()
        mViewMvc.showReorderingFinishedButton()
        NavigationController.goToReorderingDashboard()
    }

    override fun onFinishedReorderingButtonClicked() {
        mSettings.setIsReordering(false)
        //fragmentManager.beginTransaction().replace(R.id.dashboard, DashboardFragment.newInstance()).commit()
        // TODO: mViewMvc.showAppBarMenu()
        mViewMvc.hideReorderingFinishedButton()
        Log.i("SETT", "main " + mSettings.isReordering().toString())
        Log.i("SETT", "main " + mSettings.isReordering().toString())
        NavigationController.goToDashboard(0)
    }
}
