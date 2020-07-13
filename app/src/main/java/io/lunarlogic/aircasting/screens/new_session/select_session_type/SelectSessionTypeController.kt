package io.lunarlogic.aircasting.screens.new_session.select_session_type

import android.content.Context

class SelectSessionTypeController(
    private val mContext: Context?,
    private val mViewMvc: SelectSessionTypeViewMvc
) {

    fun registerListener(listener: SelectSessionTypeViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: SelectSessionTypeViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }
}