package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.util.events.DisconnectExternalSensorsEvent
import pl.llp.aircasting.ui.view.screens.common.BaseController
import org.greenrobot.eventbus.EventBus

class ConnectingAirBeamController(
    val mFragmentManager: FragmentManager
) : BaseController<ConnectingAirBeamViewMvcImpl>(null)  {
    fun onBackPressed() {
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
        mFragmentManager.popBackStack()
    }
}
