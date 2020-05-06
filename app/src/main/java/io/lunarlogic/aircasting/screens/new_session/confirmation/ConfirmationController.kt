package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context

class ConfirmationController(
    private val mContext: Context?,
    private val mViewMvc: ConfirmationViewMvc
) {

    fun registerListener(listener: ConfirmationViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: ConfirmationViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }
}