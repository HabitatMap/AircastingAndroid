package pl.llp.aircasting.ui.view.screens.session_view.map

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.ui.view.screens.common.BaseActivity
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.ResultCodes
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector
import pl.llp.aircasting.util.setupAppBar
import javax.inject.Inject

class MapActivity: BaseActivity() {
    private var controller: MapController? = null
    private val sessionsViewModel by viewModels<SessionsViewModel>()
    private val errorHandler = ErrorHandler(this)
    private var view: MapViewMvcImpl? = null

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    @Inject
    lateinit var airbeamReconnector: AirBeamReconnector

    companion object {
        val SENSOR_NAME_KEY = "SENSOR_NAME"
        val SESSION_UUID_KEY = "SESSION_UUID"
        val SESSION_TAB_KEY = "SESSION_TAB"

        fun start(
            context: Context?,
            sensorName: String?,
            sessionUUID: String,
            sessionTab: SessionsTab
        ) {
            context?.let{
                val intent = Intent(it, MapActivity::class.java)
                intent.putExtra(SENSOR_NAME_KEY, sensorName)
                intent.putExtra(SESSION_UUID_KEY, sessionUUID)
                intent.putExtra(SESSION_TAB_KEY, sessionTab.value)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as AircastingApplication)
            .appComponent.inject(this)

        val sensorName: String? = intent.extras?.get(SENSOR_NAME_KEY) as String?
        val sessionUUID: String = intent.extras?.get(SESSION_UUID_KEY) as String
        val sessionTab: Int = intent.extras?.getInt(SESSION_TAB_KEY) as Int

        view = MapViewMvcImplFactory.get(
            layoutInflater,
            null,
            supportFragmentManager,
            SessionsTab.fromInt(sessionTab)
        )
        controller = MapController(this, sessionsViewModel, view, sessionUUID, sensorName, supportFragmentManager, settings, apiServiceFactory, airbeamReconnector)

        controller?.onCreate()

        setContentView(view?.rootView)
        setupAppBar(this, topAppBar)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ResultCodes.AIRCASTING_REQUEST_LOCATION_ENABLE -> {
                if (resultCode == Activity.RESULT_OK) {
                    controller?.onLocationSettingsSatisfied()
                } else {
                    errorHandler.showError(R.string.errors_location_services_required_to_locate)
                }
            }
        }
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
    }
}