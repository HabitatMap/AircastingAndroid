package pl.llp.aircasting.ui.view.screens.session_view.map

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.SessionDownloadService
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewController
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.LocationChanged
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.location.LocationHelper
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector

open class MapController(
    rootActivity: AppCompatActivity,
    mViewMvc: SessionDetailsViewMvc?,
    sessionUUID: String,
    sensorName: String?,
    fragmentManager: FragmentManager,
    mSessionsViewModel: SessionsViewModel,
    mSettings: Settings,
    mErrorHandler: ErrorHandler,
    mApiService: ApiService,
    mDownloadService: SessionDownloadService,
    mSessionRepository: SessionsRepository,
    mMeasurementsRepository: MeasurementsRepositoryImpl,
    private val airBeamReconnector: AirBeamReconnector
) : SessionDetailsViewController(
    rootActivity,
    mViewMvc,
    fragmentManager,
    sessionUUID,
    sensorName,
    mSessionsViewModel,
    mSettings,
    mErrorHandler,
    mApiService,
    mDownloadService,
    mSessionRepository,
    mMeasurementsRepository,
),
    SessionDetailsViewMvc.Listener {
    private var mLocateRequested = false

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocationChanged) {
        if (mLocateRequested) {
            val location = LocationHelper.lastLocation()
            location?.let { mViewMvc?.centerMap(location) }
            mLocateRequested = false
        }
    }

    override fun onResume() {
        super.onResume()
        mViewMvc?.registerListener(this)
    }

    open fun onPause() {
        mViewMvc?.unregisterListener(this)
    }

    override fun locateRequested() {
        val location = LocationHelper.lastLocation()
        if (location == null) {
            requestLocation()
        } else {
            mViewMvc?.centerMap(location)
        }
    }

    private fun requestLocation() {
        mLocateRequested = true
        LocationHelper.checkLocationServicesSettings(mRootActivity)
    }

    fun onLocationSettingsSatisfied() {
        LocationHelper.start()
    }

    override fun onSessionDisconnectClicked(session: Session) {
        airBeamReconnector.disconnect(session)
        mRootActivity.finish()
    }

    override fun deleteNotePressed(
        note: Note?,
        session: Session?
    ) { // Delete session on EditNoteBottomSheet pressed
        super.deleteNotePressed(note, session)
        if (note != null) {
            mViewMvc?.deleteNote(note)
        }
    }
}
