package io.lunarlogic.aircasting.screens.main

import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.Measurement
import io.lunarlogic.aircasting.database.MeasurementStream
import io.lunarlogic.aircasting.database.Session
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
            val sessionId = db.sessions().insert(Session(UUID.randomUUID().toString(), "sesja ani", listOf("tag1", "tag2"), Date(), Date()))
            val measurementStreamId = db.measurementStreams().insert(
                MeasurementStream(sessionId, "package name", "airbeam", "pm2.5", "pm2.5", "pm2.5", "pm2.5", 1, 2, 3, 4, 5))
            db.measurements().insert(Measurement(measurementStreamId,7.77, Date()))
            db.measurements().getAll().forEach { println("ANIA VALUE:" + it.id + ", " + it.value + ", " + it.time + ", " + it.measurementStreamId) }
        }
    }

    fun onDestroy() {
        sessionManager?.unregisterFromEventBus()
        EventBus.getDefault().post(ApplicationClosed())
    }
}