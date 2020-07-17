package io.lunarlogic.aircasting.screens.new_session.confirmation

import android.content.Context
import io.lunarlogic.aircasting.events.LocationChanged
import io.lunarlogic.aircasting.lib.KeyboardHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ConfirmationController(
    private val mContext: Context?,
    private val mViewMvc: ConfirmationViewMvc
) {
    fun registerToEventBus() {
        EventBus.getDefault().register(this);
    }

    fun registerListener(listener: ConfirmationViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: ConfirmationViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }

    fun onStart(context: Context?) {
        KeyboardHelper.hideKeyboard(context)
    }

    @Subscribe
    fun onMessageEvent(event: LocationChanged) {
        mViewMvc.updateLocation(event.latitude, event.longitude)
    }
}