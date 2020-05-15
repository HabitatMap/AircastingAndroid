package io.lunarlogic.aircasting.screens.main

import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.Measurement
import io.lunarlogic.aircasting.events.ApplicationClosed
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.new_session.LoginActivity
import io.lunarlogic.aircasting.sensor.SessionManager
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import java.util.*

class MainController(private val rootActivity: AppCompatActivity, private val mViewMvc: MainViewMvc) {
    private var sessionManager: SessionManager? = null
    val settings = Settings(rootActivity)
    val databaseProvider = DatabaseProvider()
    val db = databaseProvider.get(rootActivity)

    fun onCreate() {
        if (settings.getAuthToken() == null) {
            LoginActivity.start(rootActivity)
            rootActivity.finish()
        } else {
            sessionManager = SessionManager(rootActivity, settings)
        }

        sessionManager?.registerToEventBus()


        GlobalScope.launch(Dispatchers.IO) {
            db.measurementDao().insert(Measurement(7.77, Date()))
            db.measurementDao().getAll().forEach { println("ANIA VALUE:" + it.id + ", " + it.value + ", " + it.time) }
        }
    }

    fun onDestroy() {
        sessionManager?.unregisterFromEventBus()
        EventBus.getDefault().post(ApplicationClosed())
    }
}