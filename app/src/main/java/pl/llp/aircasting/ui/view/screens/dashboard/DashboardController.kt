package pl.llp.aircasting.ui.view.screens.dashboard

import androidx.lifecycle.LifecycleCoroutineScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import pl.llp.aircasting.data.api.services.DownloadFollowedSessionMeasurementsService
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.ui.view.screens.dashboard.reordering_dashboard.BaseDashboardController
import pl.llp.aircasting.util.OperationStatus

@AssistedFactory
interface DashboardControllerFactory {
    fun create(
        mViewMvc: DashboardViewMvcImpl?,
        lifecycleCoroutineScope: LifecycleCoroutineScope,
    ): DashboardController
}

class DashboardController @AssistedInject constructor(
    @Assisted private val viewMvc: DashboardViewMvcImpl?,
    @Assisted private val lifecycleCoroutineScope: LifecycleCoroutineScope,
    private val sessionsSyncService: SessionsSyncService,
    private val downloadFollowedSessionMeasurementsService: DownloadFollowedSessionMeasurementsService
) : BaseDashboardController(viewMvc),
    DashboardViewMvc.Listener {

    override fun onCreate(tabId: Int?) {
        super.onCreate(tabId)
        viewMvc?.registerListener(this)
        lifecycleCoroutineScope.launch {
            sessionsSyncService.syncStatus
                .combine(
                    downloadFollowedSessionMeasurementsService.downloadStatus
                ) { syncStatus, downloadStatus ->
                    Pair(syncStatus, downloadStatus)
                }.collect { value ->
                    val (syncStatus, downloadStatus) = value
                    if (eitherIsInProgress(syncStatus, downloadStatus)) {
                        viewMvc?.showLoader()
                    } else if (bothAreIdle(syncStatus, downloadStatus)) {
                        viewMvc?.hideLoader()
                    }
                }
        }
    }

    private fun bothAreIdle(
        syncStatus: OperationStatus,
        downloadStatus: OperationStatus
    ) = syncStatus == OperationStatus.Idle && downloadStatus == OperationStatus.Idle

    private fun eitherIsInProgress(
        syncStatus: OperationStatus,
        downloadStatus: OperationStatus
    ) = syncStatus == OperationStatus.InProgress || downloadStatus == OperationStatus.InProgress

    override fun onRefreshTriggered() {
        lifecycleCoroutineScope.launch {
            sessionsSyncService.sync()
        }
    }
}
