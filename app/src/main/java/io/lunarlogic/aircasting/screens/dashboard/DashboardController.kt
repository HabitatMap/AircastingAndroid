package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.events.StartRecordingEvent
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.sensor.Session
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class DashboardController(
    private val mView: DashboardViewMvc
) {

    fun onCreate(tabId: Int?) {
        mView.goToTab(tabId ?: 0)
    }

    @Subscribe
    fun onMessageEvent(event: StopRecordingEvent) {
        // stop is possible only for mobile sessions
//        mView.goToTab(Session.Type.MOBILE, Session.Status.FINISHED)
    }
}
