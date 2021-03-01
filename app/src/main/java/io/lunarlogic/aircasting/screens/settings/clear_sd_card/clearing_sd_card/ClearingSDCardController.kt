package io.lunarlogic.aircasting.screens.settings.clear_sd_card.clearing_sd_card

import io.lunarlogic.aircasting.events.DisconnectExternalSensorsEvent
import org.greenrobot.eventbus.EventBus


class ClearingSDCardController {
    fun onBackPressed() {
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
    }
}
