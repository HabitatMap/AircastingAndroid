package io.lunarlogic.aircasting.screens.lets_start

import android.content.Context
import io.lunarlogic.aircasting.R
import androidx.fragment.app.FragmentActivity
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.networking.services.ConnectivityManager
import io.lunarlogic.aircasting.screens.sync.SyncActivity


class LetsStartController(
    private var mRootActivity: FragmentActivity?,
    private var mViewMvc: LetsStartViewMvc?,
    private var mContext: Context?,
    private val mErrorHandler: ErrorHandler
): LetsStartViewMvc.Listener {
    fun onCreate() {
        mViewMvc?.registerListener(this)
    }

    fun onDestroy() {
        mViewMvc?.unregisterListener(this)
        mViewMvc = null
        mRootActivity = null
        mContext = null
    }

    override fun onFixedSessionSelected() {
        if (!ConnectivityManager.isConnected(mContext)) {
            val header = mContext?.getString(R.string.fixed_session_no_internet_connection_header)
            val description = mContext?.getString(R.string.fixed_session_no_internet_connection)
            mErrorHandler.showErrorDialog(mRootActivity?.supportFragmentManager, header, description)
            return
        }

        NewSessionActivity.start(mRootActivity, Session.Type.FIXED)
    }

    override fun onMobileSessionSelected() {
        NewSessionActivity.start(mRootActivity, Session.Type.MOBILE)
    }

    override fun onSyncSelected() {
        SyncActivity.start(mRootActivity)
    }

    override fun onMoreInfoClicked() {
        mViewMvc?.showMoreInfoDialog()
    }
}
