package io.lunarlogic.aircasting.screens.lets_start

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.sensor.Session

class LetsStartController(
    private val mContext: Context?,
    private val mViewMvc: LetsStartViewMvc
): LetsStartViewMvc.Listener {

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

    override fun onMoreInfoClicked() {
        mViewMvc.showMoreInfoDialog()
    }
}
