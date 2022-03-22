package pl.llp.aircasting.screens.new_session.confirmation

import android.content.Context
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.events.LocationChanged
import pl.llp.aircasting.lib.KeyboardHelper
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.lib.safeRegister
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.common.BaseController

class ConfirmationController(
    viewMvc: ConfirmationViewMvcImpl?,
    private val mSettings: Settings
): BaseController<ConfirmationViewMvcImpl>(viewMvc), ConfirmationViewMvc.Listener {
    fun registerToEventBus() {
        EventBus.getDefault().safeRegister(this)
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
