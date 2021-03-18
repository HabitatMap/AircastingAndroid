package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.content.Context
import io.lunarlogic.aircasting.screens.common.BaseController

class TurnOffLocationServicesController(
    private val mContext: Context?,
    private var mViewMvc: TurnOffLocationServicesViewMvc?
) : BaseController(mView = mViewMvc) {
    fun registerListener(listener: TurnOffLocationServicesViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: TurnOffLocationServicesViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }
}
