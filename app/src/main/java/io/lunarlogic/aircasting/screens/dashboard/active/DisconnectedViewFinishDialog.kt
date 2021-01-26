package io.lunarlogic.aircasting.screens.dashboard.active

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.common.BaseDialog
import kotlinx.android.synthetic.main.disconnected_view_finish_session_dialog.view.*

class DisconnectedViewFinishDialog(
    mFragmentManager: FragmentManager,
    private val mListener: MobileActiveSessionViewMvc.DisconnectedViewListener,
    private val mSession: Session
) : BaseDialog(mFragmentManager) {
    private lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.disconnected_view_finish_session_dialog, null)

        mView.finish_recording_button.setOnClickListener {
            finishSessionConfirmed()
        }

        mView.cancel_button.setOnClickListener {
            dismiss()
        }

        return mView
    }

    private fun finishSessionConfirmed(){
        mListener.onSessionStopClicked(mSession)
    }
}
