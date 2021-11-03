package pl.llp.aircasting.screens.session_view.graph

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.lib.AppBar
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.models.SessionsViewModel
import pl.llp.aircasting.networking.services.ApiServiceFactory
import pl.llp.aircasting.screens.common.BaseActivity
import pl.llp.aircasting.screens.dashboard.SessionsTab
import pl.llp.aircasting.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.sensor.AirBeamReconnector
import javax.inject.Inject

class GraphActivity: BaseActivity() {
    private var controller: GraphController? = null
    private var view: SessionDetailsViewMvc? = null
    private val sessionsViewModel by viewModels<SessionsViewModel>()

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    @Inject
    lateinit var airbeamReconnector: AirBeamReconnector

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

        val sessionUUID: String = intent.extras?.get(SESSION_UUID_KEY) as String
        val sensorName: String? = intent.extras?.get(SENSOR_NAME_KEY) as String?
        val sessionTab: Int = intent.extras?.getInt(SESSION_TAB_KEY) as Int

        view = GraphViewMvcImplFactory.get(
            layoutInflater,
            null,
            supportFragmentManager,
            SessionsTab.fromInt(sessionTab)
        )
        controller = GraphController(this, sessionsViewModel, view, sessionUUID, sensorName, supportFragmentManager, settings, apiServiceFactory, airbeamReconnector)

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
