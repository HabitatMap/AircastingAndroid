package pl.llp.aircasting.ui.view.screens.dashboard

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.ui.view.common.BaseController
import pl.llp.aircasting.util.events.SessionsSyncEvent
import pl.llp.aircasting.util.extensions.safeRegister

class DashboardController(
    private val viewMvc: DashboardViewMvcImpl?,
    private val sessionsSyncService: SessionsSyncService
) : BaseController<DashboardViewMvcImpl>(viewMvc), DashboardViewMvc.Listener {

    fun onCreate(tabId: Int?) {
        viewMvc?.goToTab(tabId ?: SessionsTab.FOLLOWING.value)
        viewMvc?.registerListener(this)
        EventBus.getDefault().safeRegister(this)
    }

    override fun onRefreshTriggered() {
        sessionsSyncService.sync()
    }

    @Subscribe(sticky = true)
    fun onMessageEvent(initialSync: SessionsSyncEvent) {
        if (initialSync.inProgress)
            mViewMvc?.showLoader()
        else mViewMvc?.hideLoader()
    }
}
