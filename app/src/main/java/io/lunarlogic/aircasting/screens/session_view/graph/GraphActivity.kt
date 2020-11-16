package io.lunarlogic.aircasting.screens.session_view.graph

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import io.lunarlogic.aircasting.lib.AppBar
import io.lunarlogic.aircasting.models.SessionsViewModel

class GraphActivity: AppCompatActivity() {
    private var controller: GraphController? = null
    private val sessionsViewModel by viewModels<SessionsViewModel>()

    companion object {
        val SESSION_UUID_KEY = "SESSION_UUID"
        val SENSOR_NAME_KEY = "SENSOR_NAME"

        fun start(context: Context?, sessionUUID: String, sensorName: String?) {
            context?.let{
                val intent = Intent(it, GraphActivity::class.java)
                intent.putExtra(SESSION_UUID_KEY, sessionUUID)
                intent.putExtra(SENSOR_NAME_KEY, sensorName)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionUUID: String = intent.extras?.get(SESSION_UUID_KEY) as String
        val sensorName: String? = intent.extras?.get(SENSOR_NAME_KEY) as String?

        val view = GraphViewMvcImpl(layoutInflater, null, supportFragmentManager)
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
