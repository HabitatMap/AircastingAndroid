package io.lunarlogic.aircasting.screens.main

import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.events.ApplicationClosed
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.new_session.LoginActivity
import io.lunarlogic.aircasting.sensor.SessionManager
import org.greenrobot.eventbus.EventBus

class MainController(private val rootActivity: AppCompatActivity, private val mViewMvc: MainViewMvc) {
    private var sessionManager: SessionManager? = null
    val settings = Settings(rootActivity)

    fun onCreate() {
        if (settings.getAuthToken() == null) {
            LoginActivity.start(rootActivity)
            rootActivity.finish()
        } else {
            sessionManager = SessionManager(settings)
        }

        sessionManager?.registerToEventBus()
    }

    fun onDestroy() {
        sessionManager?.unregisterFromEventBus()
        EventBus.getDefault().post(ApplicationClosed())
    }
}