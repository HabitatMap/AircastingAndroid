package pl.llp.aircasting.ui.view.screens.session_view.graph

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.Authenticated
import pl.llp.aircasting.data.api.services.SessionDownloadService
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector
import javax.inject.Inject

class GraphControllerFactory @Inject constructor(
    private val mSessionsViewModel: SessionsViewModel,
    private val mSettings: Settings,
    @Authenticated private val mApiService: ApiService,
    private val mDownloadService: SessionDownloadService,
    private val mSessionRepository: SessionsRepository,
    private val mMeasurementsRepository: MeasurementsRepositoryImpl,
    private val mErrorHandler: ErrorHandler,
    private val airBeamReconnector: AirBeamReconnector,
    private val measurementStreamsRepository: MeasurementStreamsRepository
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
            mApiService,
            mDownloadService,
            mSessionRepository,
            mMeasurementsRepository,
            mErrorHandler,
            airBeamReconnector,
            measurementStreamsRepository
        )
        else -> GraphController(
            rootActivity,
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
            airBeamReconnector,
            measurementStreamsRepository
        )
    }
}