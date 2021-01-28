package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.screens.dashboard.active.FinishSessionConfirmationDialog
import io.lunarlogic.aircasting.screens.dashboard.active.FinishSessionListener
import kotlinx.android.synthetic.main.active_session_actions.view.*

class ActiveSessionActionsBottomSheet(
    private val mListener: Listener,
    private val mSessionPresenter: SessionPresenter?,
    private val mSupportFragmentManager: FragmentManager
): BottomSheet(mListener) {
    interface Listener: BottomSheet.Listener, FinishSessionListener {
        fun disconnectSessionPressed()
    }

    override fun layoutId(): Int {
        return R.layout.active_session_actions;
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        setupDisconnectedButton(view)
        setupStopButton(view)

        return view
    }

    private fun setupDisconnectedButton(view: View?) {
        val disconnectButton = view?.disconnect_session_button

        if (mSessionPresenter?.isDisconnectable() == true) {
            disconnectButton?.setOnClickListener {
                mListener.disconnectSessionPressed()
            }
        } else {
            disconnectButton?.visibility = View.GONE
        }
    }

    private fun setupStopButton(view: View?) {
        val stopButton = view?.stop_session_button
        stopButton?.setOnClickListener {
            FinishSessionConfirmationDialog(mSupportFragmentManager, mListener, mSessionPresenter!!.session!!).show()
        }
    }
}
