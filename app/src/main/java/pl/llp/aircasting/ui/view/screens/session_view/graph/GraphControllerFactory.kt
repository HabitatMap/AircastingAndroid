package pl.llp.aircasting.ui.view.screens.session_view.graph

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector
import javax.inject.Inject

class GraphControllerFactory @Inject constructor(
    private val mSessionsViewModel: SessionsViewModel,
    private val mSettings: Settings,
    private val mApiServiceFactory: ApiServiceFactory,
    private val airBeamReconnector: AirBeamReconnector,
) {
    fun create(
        rootActivity: AppCompatActivity,
        mViewMvc: SessionDetailsViewMvc?,
        sessionUUID: String,
        sensorName: String?,
        fragmentManager: FragmentManager,
        sessionsTab: SessionsTab,
    ) = when (sessionsTab) {
        SessionsTab.MOBILE_ACTIVE -> MobileActiveGraphController(
            rootActivity,
            mViewMvc,
            sessionUUID,
            sensorName,
            fragmentManager,
            mSessionsViewModel,
            mSettings,
            mApiServiceFactory,
            airBeamReconnector
        )
        else -> GraphController(
            rootActivity,
            mViewMvc,
            sessionUUID,
            sensorName,
            fragmentManager,
            mSettings,
            mApiServiceFactory,
            mSessionsViewModel,
            airBeamReconnector
        )
    }
}