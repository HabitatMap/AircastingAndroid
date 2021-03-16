package io.lunarlogic.aircasting.screens.sync.syncing

import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.events.DisconnectExternalSensorsEvent
import io.lunarlogic.aircasting.events.sdcard.SDCardLinesReadEvent
import io.lunarlogic.aircasting.lib.safeRegister
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class AirbeamSyncingController(
    private val mFragmentManager: FragmentManager,
    private val mView: AirbeamSyncingViewMvc
) {
    fun onBackPressed() {
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
        mFragmentManager.popBackStack()
    }

    fun onCreate() {
        EventBus.getDefault().safeRegister(this)
    }

    fun onDestroy() {
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onMessageEvent(event: SDCardLinesReadEvent) {
        val step = event.step
        mView.updateProgress(step, event.linesRead)
    }
}
