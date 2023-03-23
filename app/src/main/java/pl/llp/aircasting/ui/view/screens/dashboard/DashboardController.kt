package pl.llp.aircasting.ui.view.screens.dashboard

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.ui.view.screens.dashboard.reordering_dashboard.BaseDashboardController

class DashboardController(
    private val viewMvc: DashboardViewMvcImpl?,
    private val sessionsSyncService: SessionsSyncService
) : BaseDashboardController(viewMvc), DashboardViewMvc.Listener {

    override fun onCreate(tabId: Int?) {
        super.onCreate(tabId)
        viewMvc?.registerListener(this)
    }

    override fun onRefreshTriggered() {
        MainScope().launch {
            mViewMvc?.showLoader()
            sessionsSyncService.sync()
            mViewMvc?.hideLoader()
        }
    }
}
