package io.lunarlogic.aircasting.screens.new_session.confirmation

import android.content.Context
import io.lunarlogic.aircasting.events.LocationChanged
import io.lunarlogic.aircasting.lib.KeyboardHelper
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.lib.safeRegister
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.common.BaseController
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ConfirmationController(
    viewMvc: ConfirmationViewMvcImpl?,
    private val mSettings: Settings
): BaseController<ConfirmationViewMvcImpl>(viewMvc), ConfirmationViewMvc.Listener {
    fun registerToEventBus() {
        EventBus.getDefault().safeRegister(this);
    }

    fun registerListener(listener: ConfirmationViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: ConfirmationViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }

    fun onStart(context: Context?) {
        KeyboardHelper.hideKeyboard(context)
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewMvc?.onDestroy()
    }

    override fun onStartRecordingClicked(session: Session) {
        // do nothing
    }

    fun areMapsDisabled(): Boolean {
        return mSettings.areMapsDisabled()
    }

    @Subscribe
    fun onMessageEvent(event: LocationChanged) {
        mViewMvc?.updateLocation(event.latitude, event.longitude)
    }
}
