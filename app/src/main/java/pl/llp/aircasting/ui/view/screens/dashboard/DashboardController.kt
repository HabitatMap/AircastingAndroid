package pl.llp.aircasting.ui.view.screens.dashboard

import android.util.Log
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.ui.view.screens.dashboard.reordering_dashboard.BaseDashboardController
import pl.llp.aircasting.util.events.SessionsSyncEvent
import pl.llp.aircasting.util.extensions.safeRegister

class DashboardController(
    private val viewMvc: DashboardViewMvcImpl?,
    private val sessionsSyncService: SessionsSyncService
) : BaseDashboardController(viewMvc), DashboardViewMvc.Listener {

    override fun onCreate(tabId: Int?) {
        super.onCreate(tabId)
        viewMvc?.registerListener(this)
        EventBus.getDefault().safeRegister(this)
    }

    override fun onRefreshTriggered() {
        MainScope().launch {
            sessionsSyncService.syncAndObserve().collect {
                Log.d(this@DashboardController.TAG, "Collected result: ${it.TAG}")
            }
        }
    }

    @Subscribe(sticky = true)
    fun onMessageEvent(initialSync: SessionsSyncEvent) {
        if (initialSync.inProgress)
            mViewMvc?.showLoader()
        else mViewMvc?.hideLoader()
    }
}
