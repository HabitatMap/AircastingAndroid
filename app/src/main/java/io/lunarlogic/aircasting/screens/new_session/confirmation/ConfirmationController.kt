package io.lunarlogic.aircasting.screens.new_session.confirmation

import android.content.Context
import io.lunarlogic.aircasting.lib.KeyboardHelper

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

    fun onStart(context: Context?) {
        KeyboardHelper.hideKeyboard(context)
    }
}