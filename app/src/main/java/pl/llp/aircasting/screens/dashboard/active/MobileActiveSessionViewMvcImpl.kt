package pl.llp.aircasting.screens.dashboard.active

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.common.BottomSheet
import pl.llp.aircasting.screens.dashboard.ActiveSessionActionsBottomSheet
import pl.llp.aircasting.screens.dashboard.SessionPresenter
import pl.llp.aircasting.screens.dashboard.SessionViewMvcImpl

class MobileActiveSessionViewMvcImpl : SessionViewMvcImpl<MobileActiveSessionViewMvc.Listener>,
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

    override fun showExpandedMeasurementsTableValues() = true

    override fun bindCollapsedMeasurementsDesctription() {
        mMeasurementsDescription?.text = context.getString(R.string.session_last_sec_measurements_description)
    }

    override fun bindExpandedMeasurementsDesctription() {
        mMeasurementsDescription?.text = context.getString(R.string.session_last_sec_measurements_description)
    }

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

    override fun addNotePressed() {
        val session = mSessionPresenter?.session ?: return
        for (listener in listeners) {
            listener.addNoteClicked(session)
        }
        dismissBottomSheet()
    }

    override fun disconnectSessionPressed() {
        val session = mSessionPresenter?.session ?: return
        for (listener in listeners) {
            listener.onSessionDisconnectClicked(session)
        }
        dismissBottomSheet()
    }

    override fun onSessionReconnectClicked(session: Session) {
        val session = mSessionPresenter?.session ?: return
        for (listener in listeners) {
            listener.onSessionReconnectClicked(session)
        }
    }

    override fun onFinishSessionConfirmed(session: Session) {
        val session = mSessionPresenter?.session ?: return

        for (listener in listeners) {
            listener.onFinishSessionConfirmed(session)
        }

        dismissBottomSheet()
    }

    override fun onFinishAndSyncSessionConfirmed(session: Session) {
        val session = mSessionPresenter?.session ?: return

        for (listener in listeners) {
            listener.onFinishAndSyncSessionConfirmed(session)
        }
    }
}
