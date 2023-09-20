package pl.llp.aircasting.ui.view.screens.session_view.graph

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.SessionDownloadService
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewController
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.common.connector.AirBeamReconnector

open class GraphController(
    rootActivity: AppCompatActivity,
    mViewMvc: SessionDetailsViewMvc?,
    sessionUUID: String,
    sensorName: String?,
    fragmentManager: FragmentManager,
    mSettings: Settings,
    mSessionsViewModel: SessionsViewModel,
    mErrorHandler: ErrorHandler,
    mApiService: ApiService,
    mDownloadService: SessionDownloadService,
    mSessionRepository: SessionsRepository,
    mMeasurementsRepository: MeasurementsRepositoryImpl,
    private val airBeamReconnector: AirBeamReconnector,
    measurementStreamsRepository: MeasurementStreamsRepository
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
    measurementStreamsRepository
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
        airBeamReconnector.disconnect(session)
        mRootActivity.finish()
    }
}
