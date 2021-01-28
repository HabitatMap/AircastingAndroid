package io.lunarlogic.aircasting.screens.dashboard.active

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.screens.dashboard.ActiveSessionActionsBottomSheet
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvcImpl

class MobileActiveSessionViewMvcImpl: SessionViewMvcImpl<MobileActiveSessionViewMvc.Listener>,
    MobileActiveSessionViewMvc,
    MobileActiveSessionViewMvc.DisconnectedViewListener,
    ActiveSessionActionsBottomSheet.Listener
{

    private val mDisconnectedView: DisconnectedView
    private val mSupportFragmentManager: FragmentManager

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup,
        supportFragmentManager: FragmentManager
    ): super(inflater, parent, supportFragmentManager) {
        mDisconnectedView = DisconnectedView(context, this.rootView, supportFragmentManager, this)
        mSupportFragmentManager = supportFragmentManager
    }

    override fun showMeasurementsTableValues(): Boolean {
        return true
    }

    override fun bindCollapsedMeasurementsDesctription() {
        mMeasurementsDescription?.text = context.getString(R.string.session_last_sec_measurements_description)
    }

    override fun bindExpandedMeasurementsDesctription() {
        mMeasurementsDescription?.text = context.getString(R.string.session_last_sec_measurements_description)
    }

    override fun showExpandedMeasurementsTableValues() = true

    override fun buildBottomSheet(sessionPresenter: SessionPresenter?): BottomSheet? {
        return ActiveSessionActionsBottomSheet(this, sessionPresenter, mSupportFragmentManager)
    }

    override fun bindExpanded(sessionPresenter: SessionPresenter) {
        if (sessionPresenter.isDisconnected()) {
            mDisconnectedView.show(sessionPresenter)

            mActionsButton.visibility = View.GONE
            mCollapseSessionButton.visibility = View.GONE
            mExpandSessionButton.visibility = View.GONE
            mSessionCardLayout.background = context.getDrawable(R.drawable.top_border)

            mExpandedSessionView.visibility = View.GONE
        } else {
            mDisconnectedView.hide()

            mActionsButton.visibility = View.VISIBLE
            setExpandCollapseButton()
            mSessionCardLayout.background = null

            super.bindExpanded(sessionPresenter)
        }
    }

    override fun disconnectSessionPressed() {
        for (listener in listeners) {
            val session = mSessionPresenter?.session ?: return
            listener.onSessionDisconnectClicked(session)
        }
        dismissBottomSheet()
    }

    fun stopSessionPressed() {
        for (listener in listeners) {
            val session = mSessionPresenter?.session ?: return
            listener.onStopSessionClicked(session)
        }
        dismissBottomSheet()
    }

    override fun onSessionReconnectClicked(session: Session) {
        for (listener in listeners) {
            val session = mSessionPresenter?.session ?: return
            listener.onSessionReconnectClicked(session)
        }
    }

    override fun onStopSessionClicked(session: Session) {
        stopSessionPressed()
    }
}
