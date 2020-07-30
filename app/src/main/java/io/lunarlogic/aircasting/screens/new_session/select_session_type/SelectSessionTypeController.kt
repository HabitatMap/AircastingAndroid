package io.lunarlogic.aircasting.screens.new_session.select_session_type

import android.content.Context
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.sensor.Session

class SelectSessionTypeController(
    private val mContext: Context?,
    private val mViewMvc: SelectSessionTypeViewMvc
): SelectSessionTypeViewMvc.Listener {

    fun onStart() {
        mViewMvc.registerListener(this)
    }

    fun onStop() {
        mViewMvc.unregisterListener(this)
    }

    override fun onFixedSessionSelected() {
        NewSessionActivity.start(mContext, Session.Type.FIXED)
    }

    override fun onMobileSessionSelected() {
        NewSessionActivity.start(mContext, Session.Type.MOBILE)
    }
}
