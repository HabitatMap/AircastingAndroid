package pl.llp.aircasting.ui.view.screens.session_view.graph

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewController
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.StandaloneModeEvent
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector

open class GraphController(
    rootActivity: AppCompatActivity,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionDetailsViewMvc?,
    sessionUUID: String,
    sensorName: String?,
    fragmentManager: FragmentManager,
    mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    private val airBeamReconnector: AirBeamReconnector,
    private val permissionsManager: PermissionsManager
) : SessionDetailsViewController(
    rootActivity,
    mSessionsViewModel,
    mViewMvc,
    sessionUUID,
    sensorName,
    fragmentManager,
    mSettings,
    mApiServiceFactory
),
    SessionDetailsViewMvc.Listener {
    override fun locateRequested() {}

    override fun onResume() {
        super.onResume()
        mViewMvc?.registerListener(this)
    }

    open fun onPause() {
        mViewMvc?.unregisterListener(this)
    }

    override fun onSessionDisconnectClicked(session: Session) {
        EventBus.getDefault().post(StandaloneModeEvent(session.uuid))
        airBeamReconnector.disconnect(session)
        mRootActivity.finish()
    }
}
