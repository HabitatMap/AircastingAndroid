package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DashboardController(
    private val mContext: Context?,
    private val mViewMvc: DashboardViewMvc
) : DashboardViewMvc.Listener {

    fun onStart() {
        mViewMvc.registerListener(this)
        EventBus.getDefault().register(this);
    }

    fun onStop() {
        mViewMvc.unregisterListener(this)
        EventBus.getDefault().unregister(this);
    }

    override fun onRecordNewSessionClicked() {
        NewSessionActivity.start(mContext)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NewMeasurementEvent) {
        mViewMvc.updateMeasurements(event.measurement)
    }
}