package pl.llp.aircasting.ui.view.screens.session_view.map

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardPagerAdapter
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.NoteCreatedEvent
import pl.llp.aircasting.util.events.SensorDisconnectedEvent
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector

class MobileActiveMapController(
    activity: AppCompatActivity,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionDetailsViewMvc?,
    sessionUUID: String,
    sensorName: String?,
    mFragmentManager: FragmentManager,
    settings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    airBeamReconnector: AirBeamReconnector,
    permissionsManager: PermissionsManager,
) : MapController(
    activity,
    mSessionsViewModel,
    mViewMvc,
    sessionUUID,
    sensorName,
    mFragmentManager,
    settings,
    mApiServiceFactory,
    airBeamReconnector,
    permissionsManager
) {
    @Subscribe
    fun onMessage(event: NoteCreatedEvent) {
        mViewMvc?.addNote(event.note)
    }

    @Subscribe
    fun onMessage(event: SensorDisconnectedEvent) {
        if (event.sessionUUID == mSessionPresenter.session?.uuid)
            MainActivity.navigate(mRootActivity, DashboardPagerAdapter.MOBILE_ACTIVE_TAB_INDEX)
    }
}