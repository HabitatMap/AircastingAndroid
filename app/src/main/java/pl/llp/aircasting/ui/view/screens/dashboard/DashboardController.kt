package pl.llp.aircasting.ui.view.screens.dashboard

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.ui.view.screens.dashboard.reordering_dashboard.BaseDashboardController
import pl.llp.aircasting.util.OperationStatus

class DashboardController(
    private val viewMvc: DashboardViewMvcImpl?,
    private val sessionsSyncService: SessionsSyncService
) : BaseDashboardController(viewMvc), DashboardViewMvc.Listener {

    override fun onCreate(tabId: Int?) {
        super.onCreate(tabId)
        viewMvc?.registerListener(this)
        MainScope().launch {
            sessionsSyncService.syncStatus.collect { syncStatus ->
                when(syncStatus) {
                    SessionsSyncService.Status.InProgress -> mViewMvc?.showLoader()
                    SessionsSyncService.Status.Idle -> mViewMvc?.hideLoader()
                }
            }
        }
    }

    override fun onRefreshTriggered() {
        MainScope().launch {
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
