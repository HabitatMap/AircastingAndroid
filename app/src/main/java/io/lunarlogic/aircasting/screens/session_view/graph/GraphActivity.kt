package io.lunarlogic.aircasting.screens.session_view.graph

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.AppBar
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.screens.dashboard.SessionsTab
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvc
import javax.inject.Inject

class GraphActivity: AppCompatActivity() {
    private var controller: GraphController? = null
    private var view: SessionDetailsViewMvc? = null
    private val sessionsViewModel by viewModels<SessionsViewModel>()

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    companion object {
        val SESSION_UUID_KEY = "SESSION_UUID"
        val SENSOR_NAME_KEY = "SENSOR_NAME"
        val SESSION_TAB_KEY = "SESSION_TAB"

        fun start(context: Context?, sensorName: String?, sessionUUID: String, sessionTab: SessionsTab) {
            context?.let{
                val intent = Intent(it, GraphActivity::class.java)
                intent.putExtra(SESSION_UUID_KEY, sessionUUID)
                intent.putExtra(SENSOR_NAME_KEY, sensorName)
                intent.putExtra(SESSION_TAB_KEY, sessionTab.value)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as AircastingApplication)
            .appComponent.inject(this)

        val sessionUUID: String = intent.extras?.get(SESSION_UUID_KEY) as String
        val sensorName: String? = intent.extras?.get(SENSOR_NAME_KEY) as String?
        val sessionTab: Int = intent.extras?.getInt(SESSION_TAB_KEY) as Int

        view = GraphViewMvcImplFactory.get(
            layoutInflater,
            null,
            supportFragmentManager,
            SessionsTab.fromInt(sessionTab)
        )
        controller = GraphController(this, sessionsViewModel, view, sessionUUID, sensorName, supportFragmentManager, settings, apiServiceFactory)

        controller?.onCreate()

        setContentView(view?.rootView)
        AppBar.setup(view?.rootView, this)
    }

    override fun onResume() {
        super.onResume()

        controller?.onResume()
    }

    override fun onPause() {
        super.onPause()

        controller?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()

        AppBar.destroy()
        view?.onDestroy()
        controller?.onDestroy()
        controller = null
    }
}
