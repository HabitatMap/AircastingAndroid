package pl.llp.aircasting.ui.view.screens.session_view.map

import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector
import javax.inject.Inject

class MapControllerFactory @Inject constructor(
    private val mSessionsViewModel: SessionsViewModel,
    private val mSettings: Settings,
    private val mApiServiceFactory: ApiServiceFactory,
    private val airBeamReconnector: AirBeamReconnector
) {
    fun create(
        rootActivity: BaseActivity,
        mViewMvc: MapViewMvcImpl?,
        sessionUUID: String,
        sensorName: String?,
        supportFragmentManager: FragmentManager,
        sessionsTab: SessionsTab
    ) = when (sessionsTab) {
        SessionsTab.MOBILE_ACTIVE -> MobileActiveMapController(
            rootActivity,
            mViewMvc,
            sessionUUID,
            sensorName,
            supportFragmentManager,
            mSessionsViewModel,
            mSettings,
            mApiServiceFactory,
            airBeamReconnector
        )
        else -> MapController(
            rootActivity,
            mViewMvc,
            sessionUUID,
            sensorName,
            supportFragmentManager,
            mSessionsViewModel,
            mSettings,
            mApiServiceFactory,
            airBeamReconnector
        )
    }
}
