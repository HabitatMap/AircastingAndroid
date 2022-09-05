package pl.llp.aircasting.ui.view.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.activity_graph.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.ActiveSessionActionsBottomSheet
import pl.llp.aircasting.util.extensions.gone
import pl.llp.aircasting.util.extensions.visible

class GraphViewMobileActiveMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?
) : GraphViewMvcImpl(inflater, parent, supportFragmentManager),
    ActiveSessionActionsBottomSheet.Listener {
    private var mSessionActionsButton: ImageView? = null
    private var mBottomSheet: BottomSheet? = null

    init {
        mSessionActionsButton = rootView?.session_actions_button
        mSessionActionsButton?.visible()
        mSessionActionsButton?.setOnClickListener {
            showBottomSheet(supportFragmentManager)
        }
    }

    private fun showBottomSheet(supportFragmentManager: FragmentManager?) {
        supportFragmentManager ?: return

        mBottomSheet =
            ActiveSessionActionsBottomSheet(this, mSessionPresenter, supportFragmentManager)
        mBottomSheet?.show(supportFragmentManager)
    }

    private fun dismissBottomSheet() {
        mBottomSheet?.dismiss()
    }

    override fun bindSessionMeasurementsDescription() {
        mSessionMeasurementsDescription?.text =
            context.getString(R.string.session_last_sec_measurements_description)
    }

    override fun defaultZoomSpan(): Int {
        return 30 * 60 * 1000 // 30 minutes
    }

    override fun addNotePressed() {
        val session = mSessionPresenter?.session ?: return
        for (listener in listeners) {
            listener.addNoteClicked(session)
        }
        dismissBottomSheet()
    }

    override fun onFinishSessionConfirmed(session: Session) {
        val session = mSessionPresenter?.session ?: return

        for (listener in listeners) {
            listener.onFinishSessionConfirmed(session)
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

    override fun onFinishAndSyncSessionConfirmed(session: Session) {}

    override fun getSessionType(): Session.Type {
        return Session.Type.MOBILE
    }
}
