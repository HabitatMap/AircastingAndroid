package pl.llp.aircasting.ui.view.screens.lets_begin

import android.content.Context
import androidx.fragment.app.FragmentActivity
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.ConnectivityManager
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BaseController
import pl.llp.aircasting.ui.view.screens.new_session.NewSessionActivity
import pl.llp.aircasting.ui.view.screens.search.SearchFixedSessionActivity
import pl.llp.aircasting.ui.view.screens.sync.SyncActivity
import pl.llp.aircasting.ui.view.screens.sync.SyncUnavailableDialog
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.adjustMenuVisibility
import pl.llp.aircasting.util.isSDKLessOrEqualToNMR1

class LetsBeginController(
    private var mRootActivity: FragmentActivity?,
    viewMvc: LetsBeginViewMvcImpl?,
    private var mContext: Context?,
    private val mErrorHandler: ErrorHandler
): BaseController<LetsBeginViewMvcImpl>(viewMvc), LetsBeginViewMvc.Listener {

    fun onCreate() {
        mRootActivity?.adjustMenuVisibility(false)
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

    override fun onFollowSessionSelected() {
        SearchFixedSessionActivity.start(mRootActivity)
    }
}
