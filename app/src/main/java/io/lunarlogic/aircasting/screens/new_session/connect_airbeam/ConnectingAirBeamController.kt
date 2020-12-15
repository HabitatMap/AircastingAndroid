package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import io.lunarlogic.aircasting.events.DisconnectExternalSensorsEvent
import org.greenrobot.eventbus.EventBus

class ConnectingAirBeamController {
    fun onBackPressed() {
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
    }
}
