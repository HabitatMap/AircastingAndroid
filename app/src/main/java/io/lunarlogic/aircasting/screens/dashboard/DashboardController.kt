package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.events.StartRecordingEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class DashboardController(
    private val mView: DashboardViewMvc
) {

    fun onCreate() {
        EventBus.getDefault().register(this)
    }

    fun onDestroy() {
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onMessageEvent(event: StartRecordingEvent) {
        val sessionType = event.session.type
        mView.goToTab(sessionType)
    }
}