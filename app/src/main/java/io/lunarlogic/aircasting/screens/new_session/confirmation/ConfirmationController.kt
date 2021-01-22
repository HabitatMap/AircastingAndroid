package io.lunarlogic.aircasting.screens.new_session.confirmation

import android.content.Context
import io.lunarlogic.aircasting.events.LocationChanged
import io.lunarlogic.aircasting.lib.KeyboardHelper
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.models.Session
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ConfirmationController(
    private val mContext: Context?,
    private val mViewMvc: ConfirmationViewMvc,
    private val mSettings: Settings
): ConfirmationViewMvc.Listener {
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

        mViewMvc.recordingWithoutLocation()
    }

    override fun onStartRecordingClicked(session: Session) {
        // do nothing
    }

    override fun areMapsDisabled(): Boolean{
        return mSettings.areMapsDisabled()
    }

    @Subscribe
    fun onMessageEvent(event: LocationChanged) {
        mViewMvc.updateLocation(event.latitude, event.longitude)
    }
}
