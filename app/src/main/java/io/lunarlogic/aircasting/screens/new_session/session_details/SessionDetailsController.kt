package io.lunarlogic.aircasting.screens.new_session.session_details

import android.content.Context

open class SessionDetailsController(
    private val mContext: Context?,
    private val mViewMvc: SessionDetailsViewMvc
) {

    fun registerListener(listener: SessionDetailsViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: SessionDetailsViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }

    open fun onStart() {}
}
