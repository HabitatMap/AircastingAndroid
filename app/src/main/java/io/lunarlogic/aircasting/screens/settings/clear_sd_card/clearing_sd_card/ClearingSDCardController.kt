package io.lunarlogic.aircasting.screens.settings.clear_sd_card.clearing_sd_card

import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.events.DisconnectExternalSensorsEvent
import org.greenrobot.eventbus.EventBus


class ClearingSDCardController(
    private val mFragmentManager: FragmentManager
) {
    fun onBackPressed() {
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
        mFragmentManager.popBackStack()
    }
}
