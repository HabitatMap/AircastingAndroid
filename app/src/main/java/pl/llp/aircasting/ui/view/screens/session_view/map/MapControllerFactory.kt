package pl.llp.aircasting.ui.view.screens.session_view.map

import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.Authenticated
import pl.llp.aircasting.data.api.services.SessionDownloadService
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.common.connector.AirBeamReconnector
import javax.inject.Inject

class MapControllerFactory @Inject constructor(
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
        rootActivity: BaseActivity,
        mViewMvc: MapViewMvcImpl?,
        sessionUUID: String,
        sensorName: String?,
        supportFragmentManager: FragmentManager,
        sessionsTab: SessionsTab
    ) = when (sessionsTab) {
        SessionsTab.MOBILE_ACTIVE -> MobileActiveMapController(
            rootActivity,
            mViewMvc,
            sessionUUID,
            sensorName,
            supportFragmentManager,
            mSessionsViewModel,
            mSettings,
            mErrorHandler,
            mApiService,
            mDownloadService,
            mSessionRepository,
            mMeasurementsRepository,
            airBeamReconnector,
            measurementStreamsRepository
        )
        else -> MapController(
            rootActivity,
            mViewMvc,
            sessionUUID,
            sensorName,
            supportFragmentManager,
            mSessionsViewModel,
            mSettings,
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
