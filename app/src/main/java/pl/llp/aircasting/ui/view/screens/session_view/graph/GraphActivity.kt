package pl.llp.aircasting.ui.view.screens.session_view.graph

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.extensions.setupAppBar
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector
import javax.inject.Inject

class GraphActivity : BaseActivity() {
    private var controller: GraphController? = null
    private var view: SessionDetailsViewMvc? = null
    private val sessionsViewModel by viewModels<SessionsViewModel>()

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    @Inject
    lateinit var permissionsManager: PermissionsManager

    @Inject
    lateinit var airbeamReconnector: AirBeamReconnector

    companion object {
        val SESSION_UUID_KEY = "SESSION_UUID"
        val SENSOR_NAME_KEY = "SENSOR_NAME"
        val SESSION_TAB_KEY = "SESSION_TAB"

        fun start(
            context: Context?,
            sensorName: String?,
            sessionUUID: String,
            sessionTab: SessionsTab
        ) {
            context?.let {
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
            .userDependentComponent.inject(this)

        val sessionUUID: String = intent.extras?.get(SESSION_UUID_KEY) as String
        val sensorName: String? = intent.extras?.get(SENSOR_NAME_KEY) as String?
        val sessionTab: Int = intent.extras?.getInt(SESSION_TAB_KEY) as Int

        view = GraphViewMvcImplFactory.get(
            layoutInflater,
            null,
            supportFragmentManager,
            SessionsTab.fromInt(sessionTab)
        )
        controller = GraphControllerFactory.get(
            this,
            sessionsViewModel,
            view,
            sessionUUID,
            sensorName,
            supportFragmentManager,
            settings,
            apiServiceFactory,
            airbeamReconnector,
            permissionsManager,
            SessionsTab.fromInt(sessionTab)
        )

        controller?.onCreate()

        setContentView(view?.rootView)
        setupAppBar(this, topAppBar)
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

        view?.onDestroy()
        controller?.onDestroy()
        controller = null
    }
}
