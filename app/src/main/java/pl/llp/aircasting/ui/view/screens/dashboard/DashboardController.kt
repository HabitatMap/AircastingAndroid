package pl.llp.aircasting.ui.view.screens.dashboard

import androidx.lifecycle.LifecycleCoroutineScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.FlowCollector
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
    DashboardViewMvc.Listener,
    FlowCollector<OperationStatus> {

    override fun onCreate(tabId: Int?) {
        super.onCreate(tabId)
        viewMvc?.registerListener(this)
        lifecycleCoroutineScope.launch {
            sessionsSyncService.syncStatus.collect(this@DashboardController)
        }
        lifecycleCoroutineScope.launch {
            downloadFollowedSessionMeasurementsService.downloadStatus.collect(this@DashboardController)
        }
    }

    override fun onRefreshTriggered() {
        lifecycleCoroutineScope.launch {
            sessionsSyncService.sync()
        }
    }

    override suspend fun emit(value: OperationStatus) {
        when (value) {
            OperationStatus.InProgress -> viewMvc?.showLoader()
            OperationStatus.Idle -> viewMvc?.hideLoader()
        }
    }
}
