package io.lunarlogic.aircasting.screens.lets_start

import androidx.fragment.app.FragmentActivity
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.models.Session

class LetsStartController(
    private val mRootActivity: FragmentActivity?,
    private val mViewMvc: LetsStartViewMvc
): LetsStartViewMvc.Listener {

    fun onCreate() {
        mViewMvc.registerListener(this)
    }

    fun onDestroy() {
        mViewMvc.unregisterListener(this)
    }

    override fun onFixedSessionSelected() {
        NewSessionActivity.start(mRootActivity, Session.Type.FIXED)
    }

    override fun onMobileSessionSelected() {
        NewSessionActivity.start(mRootActivity, Session.Type.MOBILE)
    }

    override fun onMoreInfoClicked() {
        mViewMvc.showMoreInfoDialog()
    }


}
