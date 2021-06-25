package pl.llp.aircasting.screens.dashboard

import android.view.View
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.common.BottomSheet
import pl.llp.aircasting.screens.dashboard.active.FinishSessionConfirmationDialog
import pl.llp.aircasting.screens.dashboard.active.FinishSessionListener
import kotlinx.android.synthetic.main.active_session_actions.view.*

open class ActiveSessionActionsBottomSheet(
    private val mListener: Listener,
    private val mSessionPresenter: SessionPresenter?,
    private val mSupportFragmentManager: FragmentManager
) : BottomSheet() {
    interface Listener: FinishSessionListener {
        fun addNotePressed()
        fun disconnectSessionPressed()
    }

    override fun layoutId(): Int {
        return R.layout.active_session_actions;
    }

    override fun setup() {
        setupDisconnectedButton()
        setupStopButton()
        setupAddNoteButton()
        setupCancelButton()
    }

    private fun setupDisconnectedButton() {
        val disconnectButton = contentView?.disconnect_session_button

        if (mSessionPresenter?.isDisconnectable() == true) {
            disconnectButton?.setOnClickListener {
                mListener.disconnectSessionPressed()
            }
        } else {
            disconnectButton?.visibility = View.GONE
        }
    }

    private fun setupStopButton() {
        val stopButton = contentView?.stop_session_button
        val session = mSessionPresenter?.session ?: return
        stopButton?.setOnClickListener {
            dismiss()
            FinishSessionConfirmationDialog(mSupportFragmentManager, mListener, session).show()
        }
    }

    private fun setupAddNoteButton() {
        val addNoteButton = contentView?.add_note_button
        val session = mSessionPresenter?.session ?: return
        addNoteButton?.setOnClickListener {
            mListener.addNotePressed()
        }
    }

    private fun setupCancelButton() {
        val cancelButton = contentView?.cancel_button
        cancelButton?.setOnClickListener {
            dismiss()
        }
    }
}
