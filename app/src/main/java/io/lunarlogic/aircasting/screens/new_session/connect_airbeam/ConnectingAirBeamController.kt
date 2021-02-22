package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.events.DisconnectExternalSensorsEvent
import org.greenrobot.eventbus.EventBus

class ConnectingAirBeamController(
    val mFragmentManager: FragmentManager
) {
    fun onBackPressed() {
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
        mFragmentManager.popBackStack()
    }
}
