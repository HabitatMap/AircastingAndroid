package pl.llp.aircasting.ui.view.screens.session_view.map

import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector

object MapControllerFactory {
    fun get(
        rootActivity: BaseActivity,
        sessionsViewModel: SessionsViewModel,
        view: MapViewMvcImpl?,
        sessionUUID: String,
        sensorName: String?,
        supportFragmentManager: FragmentManager,
        settings: Settings,
        apiServiceFactory: ApiServiceFactory,
        airbeamReconnector: AirBeamReconnector,
        permissionsManager: PermissionsManager,
        sessionsTab: SessionsTab
    ) = when(sessionsTab) {
        SessionsTab.MOBILE_ACTIVE -> MobileActiveMapController(
            rootActivity,
            sessionsViewModel,
            view,
            sessionUUID,
            sensorName,
            supportFragmentManager,
            settings,
            apiServiceFactory,
            airbeamReconnector,
            permissionsManager
        )
        else -> MapController(
            rootActivity,
            sessionsViewModel,
            view,
            sessionUUID,
            sensorName,
            supportFragmentManager,
            settings,
            apiServiceFactory,
            airbeamReconnector,
            permissionsManager
        )
    }
}