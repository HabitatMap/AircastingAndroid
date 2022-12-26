package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionViewMvcImpl
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.mobile.active.MobileActiveSessionActionsBottomSheet

class MobileActiveSessionViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup,
    supportFragmentManager: FragmentManager
) : SessionViewMvcImpl<MobileActiveSessionViewMvc.Listener>(
    inflater,
    parent,
    supportFragmentManager
),
    MobileActiveSessionViewMvc,
    MobileActiveSessionViewMvc.DisconnectedViewListener,
    MobileActiveSessionActionsBottomSheet.Listener {

    private val mDisconnectedView: DisconnectedView =
        DisconnectedView(context, this.rootView, supportFragmentManager, this)
    private val mSupportFragmentManager: FragmentManager = supportFragmentManager

    override fun showMeasurementsTableValues(): Boolean {
        return true
    }

    override fun showExpandedMeasurementsTableValues() = true

    override fun bindCollapsedMeasurementsDescription() {
        mMeasurementsDescription?.text =
            context.getString(R.string.session_last_sec_measurements_description)
    }

    override fun bindExpandedMeasurementsDescription() {
        mMeasurementsDescription?.text =
            context.getString(R.string.session_last_sec_measurements_description)
    }

    override fun showChart() = true

    override fun bindExpanded() {
        if (mSessionPresenter?.isDisconnected() == true) {
            mDisconnectedView.show(mSessionPresenter)

            mActionsButton.visibility = View.GONE
            mCollapseSessionButton.visibility = View.GONE
            mExpandSessionButton.visibility = View.GONE
            mSessionCardLayout.background =
                context.let { AppCompatResources.getDrawable(it, R.drawable.top_border) }
            mExpandedSessionView.visibility = View.GONE
        } else {
            mDisconnectedView.hide()

            mActionsButton.visibility = View.VISIBLE
            setExpandCollapseButton()
            mSessionCardLayout.background = null

            super.bindExpanded()
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
