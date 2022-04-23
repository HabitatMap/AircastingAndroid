package pl.llp.aircasting.screens.lets_begin

import android.content.Context
import androidx.fragment.app.FragmentActivity
import pl.llp.aircasting.R
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.lib.isSDKLessOrEqualToNMR1
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.networking.services.ConnectivityManager
import pl.llp.aircasting.screens.common.BaseController
import pl.llp.aircasting.screens.new_session.NewSessionActivity
import pl.llp.aircasting.screens.sync.SyncActivity
import pl.llp.aircasting.screens.sync.SyncUnavailableDialog


class LetsBeginController(
    private var mRootActivity: FragmentActivity?,
    private var viewMvc: LetsBeginViewMvcImpl?,
    private var mContext: Context?,
    private val mErrorHandler: ErrorHandler
): BaseController<LetsBeginViewMvcImpl>(viewMvc), LetsBeginViewMvc.Listener {

    fun onCreate() {
        mViewMvc?.registerListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewMvc?.unregisterListener(this)
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
        if (isSDKLessOrEqualToNMR1()) {
            mRootActivity?.supportFragmentManager?.let { fragmentManager ->
                SyncUnavailableDialog(
                    fragmentManager
                ).show()
            }
        } else {
            SyncActivity.start(mRootActivity)
        }
    }

    override fun onMoreInfoClicked() {
        mViewMvc?.showMoreInfoDialog()
    }
}
