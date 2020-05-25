package io.lunarlogic.aircasting.screens.lets_start

import android.content.Context
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity

class LetsStartController(
    private val mContext: Context?,
    private val mViewMvc: LetsStartViewMvc): LetsStartViewMvc.Listener  {

    fun onCreate() {
        mViewMvc.registerListener(this)
    }

    override fun onRecordNewSessionClicked() {
        NewSessionActivity.start(mContext)
    }
}