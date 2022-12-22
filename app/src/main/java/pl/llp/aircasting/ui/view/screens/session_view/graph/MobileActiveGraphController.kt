package pl.llp.aircasting.ui.view.screens.session_view.graph

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.active.FinishMobileSessionListener
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector

class MobileActiveGraphController(
    rootActivity: AppCompatActivity,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionDetailsViewMvc?,
    sessionUUID: String,
    sensorName: String?,
    fragmentManager: FragmentManager,
    mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    airBeamReconnector: AirBeamReconnector,
    permissionsManager: PermissionsManager
) : GraphController(
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
),
    FinishMobileSessionListener {
    override val settings: Settings = mSettings
    override val activity: Activity = rootActivity

    override fun onFinishSessionConfirmed(session: Session) {
        super<GraphController>.onFinishSessionConfirmed(session)
        onFinishMobileSessionConfirmed()
    }
}