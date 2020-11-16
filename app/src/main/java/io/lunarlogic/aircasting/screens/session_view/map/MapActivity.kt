package io.lunarlogic.aircasting.screens.session_view.map

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.AppBar
import io.lunarlogic.aircasting.lib.ResultCodes
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel

class MapActivity: AppCompatActivity() {
    private var controller: MapController? = null
    private val sessionsViewModel by viewModels<SessionsViewModel>()
    private val errorHandler = ErrorHandler(this)

    companion object {
        val SENSOR_NAME_KEY = "SENSOR_NAME"
        val SESSION_UUID_KEY = "SESSION_UUID"
        val SESSION_TYPE_KEY = "SESSION_TYPE"
        val SESSION_STATUS_KEY = "SESSION_STATUS"

        fun start(
            context: Context?,
            sensorName: String?,
            sessionUUID: String,
            sessionType: Session.Type,
            sessionStatus: Session.Status
        ) {
            context?.let{
                val intent = Intent(it, MapActivity::class.java)
                intent.putExtra(SENSOR_NAME_KEY, sensorName)
                intent.putExtra(SESSION_UUID_KEY, sessionUUID)
                intent.putExtra(SESSION_TYPE_KEY, sessionType.value)
                intent.putExtra(SESSION_STATUS_KEY, sessionStatus.value)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sensorName: String? = intent.extras?.get(SENSOR_NAME_KEY) as String?
        val sessionUUID: String = intent.extras?.get(SESSION_UUID_KEY) as String
        val sessionType: Int = intent.extras?.getInt(SESSION_TYPE_KEY) as Int
        val sessionStatus: Int = intent.extras?.getInt(SESSION_STATUS_KEY) as Int

        val view = MapViewMvcImplFactory.get(
            layoutInflater,
            null,
            supportFragmentManager,
            Session.Type.fromInt(sessionType),
            Session.Status.fromInt(sessionStatus)
        )
        controller = MapController(this, sessionsViewModel, view, sessionUUID, sensorName)

        controller?.onCreate()

        setContentView(view.rootView)
        AppBar.setup(view.rootView, this)
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

    override fun onDestroy() {
        super.onDestroy()

        controller?.onDestroy()
    }
}
