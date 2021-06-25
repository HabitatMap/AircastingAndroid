package pl.llp.aircasting.screens.new_session.connect_airbeam

import android.content.Context

class TurnOnLocationServicesController(
    private val mContext: Context?,
    private val mViewMvc: TurnOnLocationServicesViewMvc
) {

    fun registerListener(listener: TurnOnLocationServicesViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: TurnOnLocationServicesViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }
}
