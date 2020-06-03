package io.lunarlogic.aircasting.screens.main

import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.events.ApplicationClosed
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.ConnectivityManager
import io.lunarlogic.aircasting.screens.new_session.LoginActivity
import io.lunarlogic.aircasting.sensor.SessionManager
import org.greenrobot.eventbus.EventBus

class MainController(private val rootActivity: AppCompatActivity, private val mViewMvc: MainViewMvc) {
    private var mSessionManager: SessionManager? = null
    private val mSettings = Settings(rootActivity)
    private var mConnectivityManager: ConnectivityManager? = null

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
        EventBus.getDefault().post(ApplicationClosed())
    }

    private fun showLoginScreen() {
        LoginActivity.start(rootActivity)
        rootActivity.finish()
    }

    private fun setupDashboard() {
        val apiService =  ApiServiceFactory.get(mSettings.getAuthToken()!!)
        mSessionManager = SessionManager(rootActivity, apiService)

        mConnectivityManager = ConnectivityManager(apiService, rootActivity)
        registerConnectivityManager()
    }

    private fun registerConnectivityManager() {
        val filter = IntentFilter(ConnectivityManager.ACTION)
        mConnectivityManager?.let { rootActivity.registerReceiver(it, filter) }
    }

    private fun unregisterConnectivityManager() {
        mConnectivityManager?.let { rootActivity.unregisterReceiver(it) }
    }
}