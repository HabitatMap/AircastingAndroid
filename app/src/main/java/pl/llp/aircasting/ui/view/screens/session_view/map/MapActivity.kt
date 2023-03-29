package pl.llp.aircasting.ui.view.screens.session_view.map

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.util.ResultCodes
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.setupAppBar
import javax.inject.Inject

class MapActivity : BaseActivity() {
    private lateinit var controller: MapController
    private val errorHandler = ErrorHandler(this)
    private lateinit var view: MapViewMvcImpl

    @Inject
    lateinit var controllerFactory: MapControllerFactory

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
            context?.let {
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
            .userDependentComponent?.inject(this)

        val sensorName = intent.getStringExtra(SENSOR_NAME_KEY)
        val sessionUUID: String = intent.getStringExtra(SESSION_UUID_KEY) as String
        val sessionTab: Int = intent.getIntExtra(SESSION_TAB_KEY, 0)

        view = MapViewMvcImplFactory.get(
            layoutInflater,
            null,
            supportFragmentManager,
            SessionsTab.fromInt(sessionTab)
        )
        controller = controllerFactory.create(
            this,
            view,
            sessionUUID,
            sensorName,
            supportFragmentManager,
            SessionsTab.fromInt(sessionTab)
        )

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
