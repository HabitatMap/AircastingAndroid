package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.content.Context

class TurnOnBluetoothController(
    private val mContext: Context?,
    private val mViewMvc: TurnOnBluetoothViewMvc
) {

    fun registerListener(listener: TurnOnBluetoothViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: TurnOnBluetoothViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }
}