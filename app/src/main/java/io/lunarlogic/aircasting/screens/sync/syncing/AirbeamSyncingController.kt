package io.lunarlogic.aircasting.screens.sync.syncing

import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.events.DisconnectExternalSensorsEvent
import io.lunarlogic.aircasting.events.sdcard.SDCardLinesReadEvent
import io.lunarlogic.aircasting.lib.safeRegister
import io.lunarlogic.aircasting.screens.common.BaseController
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class AirbeamSyncingController(
    viewMvc: AirbeamSyncingViewMvcImpl?,
    private val mFragmentManager: FragmentManager
) : BaseController<AirbeamSyncingViewMvcImpl>(viewMvc) {
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
}
