package pl.llp.aircasting.screens.settings.clear_sd_card.clearing_sd_card

import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.events.DisconnectExternalSensorsEvent
import pl.llp.aircasting.screens.common.BaseController
import org.greenrobot.eventbus.EventBus


class ClearingSDCardController(
    private val mFragmentManager: FragmentManager
) : BaseController<ClearingSDCardViewMvcImpl>(null){
    fun onBackPressed() {
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
        mFragmentManager.popBackStack()
    }
}
