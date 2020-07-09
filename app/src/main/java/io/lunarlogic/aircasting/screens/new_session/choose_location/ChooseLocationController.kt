package io.lunarlogic.aircasting.screens.new_session.choose_location

import android.content.Context

class ChooseLocationController(
    private val mContext: Context?,
    private val mViewMvc: ChooseLocationViewMvc
) {

    fun registerListener(listener: ChooseLocationViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: ChooseLocationViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }
}