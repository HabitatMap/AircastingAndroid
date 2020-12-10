package io.lunarlogic.aircasting.screens.main

import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.events.DisconnectExternalSensorsEvent
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiService

import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.ConnectivityManager
import io.lunarlogic.aircasting.networking.services.SessionsSyncService
import io.lunarlogic.aircasting.screens.new_session.LoginActivity
import io.lunarlogic.aircasting.sensor.SessionManager
import org.greenrobot.eventbus.EventBus

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
        if (mSettings.getAuthToken() == null) {
            showLoginScreen()
        } else {
            setupDashboard()
        }

        mSessionManager?.onStart()
    }

    fun onDestroy() {
        unregisterConnectivityManager()
        mSessionManager?.onStop()
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
    }

    private fun showLoginScreen() {
        LoginActivity.start(rootActivity)
        rootActivity.finish()
    }

    private fun setupDashboard() {
        mErrorHandler.registerUser(mSettings.getEmail())

        val apiService =  mApiServiceFactory.get(mSettings.getAuthToken()!!)
        mSessionManager = SessionManager(rootActivity, apiService)

        mConnectivityManager = ConnectivityManager(apiService, rootActivity)
        registerConnectivityManager()

        sync(apiService)
    }

    private fun sync(apiService: ApiService) {
        val mMobileSessionsSyncService = SessionsSyncService.get(apiService, mErrorHandler)

        mMobileSessionsSyncService.sync({
            mViewMvc.showLoader()
        }, {
            mViewMvc.hideLoader()
        })
    }

    private fun registerConnectivityManager() {
        val filter = IntentFilter(ConnectivityManager.ACTION)
        mConnectivityManager?.let { rootActivity.registerReceiver(it, filter) }
    }

    private fun unregisterConnectivityManager() {
        mConnectivityManager?.let { rootActivity.unregisterReceiver(it) }
    }
}
