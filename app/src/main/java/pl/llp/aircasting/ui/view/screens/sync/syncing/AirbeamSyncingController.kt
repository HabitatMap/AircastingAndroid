package pl.llp.aircasting.ui.view.screens.sync.syncing

import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.ui.view.common.BaseController
import pl.llp.aircasting.util.events.DisconnectExternalSensorsEvent
import pl.llp.aircasting.util.events.sdcard.SDCardLinesReadEvent
import pl.llp.aircasting.util.events.sdcard.SDCardSyncFinished
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SDCardSyncError
import pl.llp.aircasting.util.extensions.safeRegister

class AirbeamSyncingController(
    viewMvc: AirbeamSyncingViewMvcImpl?,
    private val mFragmentManager: FragmentManager,
    private val mErrorHandler: ErrorHandler
) : BaseController<AirbeamSyncingViewMvcImpl>(viewMvc) {
    fun registerListener(listener: AirbeamSyncingViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun onBackPressed() {
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
        mFragmentManager.popBackStack()
    }

    fun onCreate() {
        EventBus.getDefault().safeRegister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onMessageEvent(event: SDCardLinesReadEvent) {
        val step = event.step
        mViewMvc?.updateProgress(step, event.linesRead)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: SDCardSyncFinished) {
        mErrorHandler.handle(SDCardSyncError("finishSync, calling listener"))
        mViewMvc?.finishSync()
    }
}
