package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.content.Context
import io.lunarlogic.aircasting.screens.common.BaseController
import io.lunarlogic.aircasting.screens.settings.SettingsViewMvcImpl

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
