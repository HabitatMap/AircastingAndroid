package io.lunarlogic.aircasting.screens.lets_start

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import kotlinx.android.synthetic.main.fragment_lets_start.view.*

class LetsStartViewMvcImpl: BaseObservableViewMvc<LetsStartViewMvc.Listener>,
    LetsStartViewMvc, MoreInfoBottomSheet.Listener {
    private val mSupportFragmentManager: FragmentManager
    private var mDialog: MoreInfoBottomSheet? = null

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager,
        isSDCardSyncEnabled: Boolean
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_lets_start, parent, false)
        mSupportFragmentManager = supportFragmentManager

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

        if (isSDCardSyncEnabled) {
            val orLabel = rootView?.or
            orLabel?.visibility = View.VISIBLE

            val syncCard = rootView?.sync_card
            syncCard?.visibility = View.VISIBLE
            syncCard?.setOnClickListener {
                onSyncSelected()
            }

            val clearCard = rootView?.clear_card
            clearCard?.visibility = View.VISIBLE
            clearCard?.setOnClickListener {
                onClearSDCardSelected()
            }
        }
    }

    override fun showMoreInfoDialog() {
        mDialog = MoreInfoBottomSheet(this)
        mDialog?.show(mSupportFragmentManager)
    }

    override fun closePressed() {
        mDialog?.dismiss()
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

    private fun onClearSDCardSelected() {
        for (listener in listeners) {
            listener.onClearSDCardSelected()
        }
    }

    private fun onMoreInfoClicked() {
        for (listener in listeners) {
            listener.onMoreInfoClicked()
        }
    }
}
