package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SelectDeviceTypeController(
    private val mContext: Context?,
    private val mViewMvc: SelectDeviceTypeViewMvc
) {

    fun registerListener(listener: SelectDeviceTypeViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: SelectDeviceTypeViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }
}