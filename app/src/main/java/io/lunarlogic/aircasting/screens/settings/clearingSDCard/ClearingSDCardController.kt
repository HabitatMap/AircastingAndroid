package io.lunarlogic.aircasting.screens.settings.clearingSDCard

import io.lunarlogic.aircasting.events.DisconnectExternalSensorsEvent
import org.greenrobot.eventbus.EventBus


class ClearingSDCardController {
    fun onBackPressed() {
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
    }
}
