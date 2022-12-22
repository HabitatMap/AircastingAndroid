package pl.llp.aircasting.ui.view.screens.session_view.graph

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector

object GraphControllerFactory {
    fun get(
        rootActivity: AppCompatActivity,
        mSessionsViewModel: SessionsViewModel,
        mViewMvc: SessionDetailsViewMvc?,
        sessionUUID: String,
        sensorName: String?,
        fragmentManager: FragmentManager,
        mSettings: Settings,
        mApiServiceFactory: ApiServiceFactory,
        airBeamReconnector: AirBeamReconnector,
        permissionsManager: PermissionsManager,
        sessionsTab: SessionsTab
    ) = when (sessionsTab) {
        SessionsTab.MOBILE_ACTIVE -> MobileActiveGraphController(
            rootActivity,
            mSessionsViewModel,
            mViewMvc,
            sessionUUID,
            sensorName,
            fragmentManager,
            mSettings,
            mApiServiceFactory,
            airBeamReconnector,
            permissionsManager
        )
        else -> GraphController(
            rootActivity,
            mSessionsViewModel,
            mViewMvc,
            sessionUUID,
            sensorName,
            fragmentManager,
            mSettings,
            mApiServiceFactory,
            airBeamReconnector,
            permissionsManager
        )
    }
}