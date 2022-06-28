package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import android.content.Context
import pl.llp.aircasting.ui.view.common.BaseController

class TurnOffLocationServicesController(
    private val mContext: Context?,
    var viewMvc: TurnOffLocationServicesViewMvcImpl?
) :  BaseController<TurnOffLocationServicesViewMvcImpl>(viewMvc) {
    fun registerListener(listener: TurnOffLocationServicesViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: TurnOffLocationServicesViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }
}
