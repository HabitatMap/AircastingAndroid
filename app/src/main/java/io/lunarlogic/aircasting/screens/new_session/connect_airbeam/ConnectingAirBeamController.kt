package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.widget.Toast
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.events.DisconnectExternalSensorsEvent
import io.lunarlogic.aircasting.screens.common.BaseController
import io.lunarlogic.aircasting.screens.sync.synced.AirbeamSyncedViewMvcImpl
import org.greenrobot.eventbus.EventBus

class ConnectingAirBeamController(
    val mFragmentManager: FragmentManager
) : BaseController<ConnectingAirBeamViewMvcImpl>(null)  {
    fun onBackPressed() {
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
        mFragmentManager.popBackStack()
    }
}
