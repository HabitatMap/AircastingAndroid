package io.lunarlogic.aircasting.screens.lets_start

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class LetsStartViewMvcImpl: BaseObservableViewMvc<LetsStartViewMvc.Listener>,
    LetsStartViewMvc, MoreInfoBottomSheet.Listener {
    private val mSupportFragmentManager: FragmentManager
    private var mDialog: MoreInfoBottomSheet? = null

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_lets_start, parent, false)
        mSupportFragmentManager = supportFragmentManager

        val fixedSessionCard = rootView?.findViewById<CardView>(R.id.fixed_session_start_card)
        fixedSessionCard?.setOnClickListener {
            onFixedSessionSelected()
        }

        val mobileSessionCard = rootView?.findViewById<CardView>(R.id.mobile_session_start_card)
        mobileSessionCard?.setOnClickListener {
            onMobileSessionSelected()
        }

        val moreInfoButton = rootView?.findViewById<Button>(R.id.new_session_more_info)
        moreInfoButton?.setOnClickListener {
            onMoreInfoClicked()
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

    private fun onMoreInfoClicked() {
        for (listener in listeners) {
            listener.onMoreInfoClicked()
        }
    }
}
