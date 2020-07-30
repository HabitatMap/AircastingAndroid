package io.lunarlogic.aircasting.screens.lets_start

import androidx.fragment.app.FragmentActivity
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity

class LetsStartController(
    private val mActivity: FragmentActivity?,
    private val mViewMvc: LetsStartViewMvc): LetsStartViewMvc.Listener  {

    fun onCreate() {
        mViewMvc.registerListener(this)
    }

    override fun onRecordNewSessionClicked() {
        mActivity?.supportFragmentManager?.popBackStack()
//        NewSessionActivity.start(mActivity)
    }
}
