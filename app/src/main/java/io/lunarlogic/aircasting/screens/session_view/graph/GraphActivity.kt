package io.lunarlogic.aircasting.screens.session_view.graph

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import io.lunarlogic.aircasting.lib.AppBar
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel

class GraphActivity: AppCompatActivity() {
    private var controller: GraphController? = null
    private val sessionsViewModel by viewModels<SessionsViewModel>()

    companion object {
        val SESSION_UUID_KEY = "SESSION_UUID"
        val SENSOR_NAME_KEY = "SENSOR_NAME"
        val SESSION_TYPE_KEY = "SESSION_TYPE"
        val SESSION_STATUS_KEY = "SESSION_STATUS"

        fun start(context: Context?, sensorName: String?, sessionUUID: String, sessionType: Session.Type, sessionStatus: Session.Status) {
            context?.let{
                val intent = Intent(it, GraphActivity::class.java)
                intent.putExtra(SESSION_UUID_KEY, sessionUUID)
                intent.putExtra(SENSOR_NAME_KEY, sensorName)
                intent.putExtra(SESSION_TYPE_KEY, sessionType.value)
                intent.putExtra(SESSION_STATUS_KEY, sessionStatus.value)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionUUID: String = intent.extras?.get(SESSION_UUID_KEY) as String
        val sensorName: String? = intent.extras?.get(SENSOR_NAME_KEY) as String?
        val sessionType: Int = intent.extras?.getInt(SESSION_TYPE_KEY) as Int
        val sessionStatus: Int = intent.extras?.getInt(SESSION_STATUS_KEY) as Int

        val view = GraphViewMvcImplFactory.get(
            layoutInflater,
            null,
            supportFragmentManager,
            Session.Type.fromInt(sessionType),
            Session.Status.fromInt(sessionStatus)
        )
        controller = GraphController(this, sessionsViewModel, view, sessionUUID, sensorName)

        controller?.onCreate()

        setContentView(view.rootView)
        AppBar.setup(view.rootView, this)
    }

    override fun onDestroy() {
        super.onDestroy()

        controller?.onDestroy()
    }
}
