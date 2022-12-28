package pl.llp.aircasting.ui.view.screens.session_view.graph

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.NoteCreatedEvent
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector

class MobileActiveGraphController(
    mRootActivity: AppCompatActivity,
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
    mRootActivity,
    mSessionsViewModel,
    mViewMvc,
    sessionUUID,
    sensorName,
    fragmentManager,
    mSettings,
    mApiServiceFactory,
    airBeamReconnector,
    permissionsManager
) {
    @Subscribe
    fun onMessage(event: NoteCreatedEvent) {
        mViewMvc?.addNote(event.note)
    }
}