package pl.llp.aircasting.ui.view.screens.session_view.graph

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.ActiveSessionActionsBottomSheet
import kotlinx.android.synthetic.main.activity_graph.view.*

class GraphViewMobileActiveMvcImpl : GraphViewMvcImpl,
    ActiveSessionActionsBottomSheet.Listener {
    private var mSessionActionsButton: ImageView? = null
    private var mBottomSheet: BottomSheet? = null

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager?
    ): super(inflater, parent, supportFragmentManager) {
        mSessionActionsButton = rootView?.session_actions_button
        mSessionActionsButton?.visibility = View.VISIBLE

        mSessionActionsButton?.setOnClickListener {
            showBottomSheet(supportFragmentManager)
        }
    }

    fun showBottomSheet(supportFragmentManager: FragmentManager?) {
        supportFragmentManager ?: return

        mBottomSheet = ActiveSessionActionsBottomSheet(this, mSessionPresenter, supportFragmentManager)
        mBottomSheet?.show(supportFragmentManager)
    }

    fun dismissBottomSheet() {
        mBottomSheet?.dismiss()
    }

    override fun bindSessionMeasurementsDescription() {
        mSessionMeasurementsDescription?.text = context.getString(R.string.session_last_sec_measurements_description)
    }

    override fun defaultZoomSpan(): Int? {
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

    override fun onFinishAndSyncSessionConfirmed(session: Session) {
        // do nothing
    }

    override fun getSessionType(): Session.Type {
        return Session.Type.MOBILE
    }
}
