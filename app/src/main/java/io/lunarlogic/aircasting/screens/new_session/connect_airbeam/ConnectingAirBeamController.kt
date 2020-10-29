package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.content.Context
import io.lunarlogic.aircasting.events.DisconnectExternalSensorsEvent
import io.lunarlogic.aircasting.sensor.AirBeamConnector
import org.greenrobot.eventbus.EventBus

class ConnectingAirBeamController {
    fun onBackPressed() {
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
    }
}
