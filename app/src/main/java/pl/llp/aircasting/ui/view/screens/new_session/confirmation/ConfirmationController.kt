package pl.llp.aircasting.ui.view.screens.new_session.confirmation

import android.content.Context
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.util.events.LocationChanged
import pl.llp.aircasting.util.KeyboardHelper
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.safeRegister
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.common.BaseController

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