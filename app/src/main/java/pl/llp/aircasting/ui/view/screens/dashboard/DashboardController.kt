package pl.llp.aircasting.ui.view.screens.dashboard

import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.ui.view.common.BaseController

class DashboardController(
    private val viewMvc: DashboardViewMvcImpl?,
    private val sessionsSyncService: SessionsSyncService
) : BaseController<DashboardViewMvcImpl>(viewMvc), DashboardViewMvc.Listener,
    SessionsSyncService.Listener {

    fun onCreate(tabId: Int?) {
        viewMvc?.goToTab(tabId ?: SessionsTab.FOLLOWING.value)
        viewMvc?.registerListener(this)
        sessionsSyncService.registerListener(this)
        mViewMvc?.showLoader()
        sessionsSyncService.sync()
    }

    override fun onRefreshTriggered() {
        sessionsSyncService.sync()
    }

    override fun onSyncFinished() {
        mViewMvc?.hideLoader()
    }
}
