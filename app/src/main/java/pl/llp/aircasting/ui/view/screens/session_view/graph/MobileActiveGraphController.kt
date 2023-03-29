package pl.llp.aircasting.ui.view.screens.session_view.graph

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.SessionDownloadService
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardPagerAdapter
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.NoteCreatedEvent
import pl.llp.aircasting.util.events.SensorDisconnectedEvent
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector

class MobileActiveGraphController(
    mRootActivity: AppCompatActivity,
    mViewMvc: SessionDetailsViewMvc?,
    sessionUUID: String,
    sensorName: String?,
    fragmentManager: FragmentManager,
    mSessionsViewModel: SessionsViewModel,
    mSettings: Settings,
    mApiService: ApiService,
    mDownloadService: SessionDownloadService,
    mSessionRepository: SessionsRepository,
    mMeasurementsRepository: MeasurementsRepositoryImpl,
    mErrorHandler: ErrorHandler,
    airBeamReconnector: AirBeamReconnector
) : GraphController(
    mRootActivity,
    mViewMvc,
    sessionUUID,
    sensorName,
    fragmentManager,
    mSettings,
    mSessionsViewModel,
    mErrorHandler,
    mApiService,
    mDownloadService,
    mSessionRepository,
    mMeasurementsRepository,
    airBeamReconnector
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