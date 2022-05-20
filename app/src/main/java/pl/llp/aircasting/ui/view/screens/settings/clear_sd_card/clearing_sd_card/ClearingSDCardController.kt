package pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.clearing_sd_card

import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.util.events.DisconnectExternalSensorsEvent
import pl.llp.aircasting.ui.view.common.BaseController
import org.greenrobot.eventbus.EventBus


class ClearingSDCardController(
    private val mFragmentManager: FragmentManager
) : BaseController<ClearingSDCardViewMvcImpl>(null){
    fun onBackPressed() {
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
        mFragmentManager.popBackStack()
    }
}
