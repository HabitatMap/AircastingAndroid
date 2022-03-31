package pl.llp.aircasting.screens.lets_start

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.fragment_lets_start.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.common.BaseObservableViewMvc

class LetsStartViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager
) : BaseObservableViewMvc<LetsStartViewMvc.Listener>(),
    LetsStartViewMvc {
    private val mSupportFragmentManager: FragmentManager = supportFragmentManager
    private var mDialog: MoreInfoBottomSheet? = null

    init {
        this.rootView = inflater.inflate(R.layout.fragment_lets_start, parent, false)
        val fixedSessionCard = rootView?.fixed_session_start_card
        fixedSessionCard?.setOnClickListener {
            onFixedSessionSelected()
        }
        val mobileSessionCard = rootView?.mobile_session_start_card
        mobileSessionCard?.setOnClickListener {
            onMobileSessionSelected()
        }
        val moreInfoButton = rootView?.new_session_more_info
        moreInfoButton?.setOnClickListener {
            onMoreInfoClicked()
        }
        val syncCard = rootView?.sync_card
        syncCard?.visibility = View.VISIBLE
        syncCard?.setOnClickListener {
            onSyncSelected()
        }
    }

    override fun showMoreInfoDialog() {
        mDialog = MoreInfoBottomSheet()
        mDialog?.show(mSupportFragmentManager)
    }

    private fun onFixedSessionSelected() {
        for (listener in listeners) {
            listener.onFixedSessionSelected()
        }
    }

    private fun onMobileSessionSelected() {
        for (listener in listeners) {
            listener.onMobileSessionSelected()
        }
    }

    private fun onSyncSelected() {
        for (listener in listeners) {
            listener.onSyncSelected()
        }
    }

    private fun onMoreInfoClicked() {
        for (listener in listeners) {
            listener.onMoreInfoClicked()
        }
    }
}
