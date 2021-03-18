package io.lunarlogic.aircasting.screens.session_view.graph

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.dashboard.ActiveSessionActionsBottomSheet
import kotlinx.android.synthetic.main.activity_graph.view.*

class GraphViewMobileActiveMvcImpl : GraphViewMvcImpl,
    ActiveSessionActionsBottomSheet.Listener {
    private var mSessionActionsButton: ImageView? = null

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager?
    ): super(inflater, parent, supportFragmentManager) {
        mSessionActionsButton = rootView?.session_actions_button
        mSessionActionsButton?.visibility = View.VISIBLE

        mSessionActionsButton?.setOnClickListener {
            if (supportFragmentManager != null) { // todo: this null check to be changed <??>
                ActiveSessionActionsBottomSheet(this, mSessionPresenter, supportFragmentManager).show(supportFragmentManager)
            }
        }
    }

    override fun bindSessionMeasurementsDescription() {
        mSessionMeasurementsDescription?.text = context.getString(R.string.session_last_sec_measurements_description)
    }

    override fun defaultZoomSpan(): Int? {
        return 30 * 60 * 1000 // 30 minutes
    }

    override fun addNotePressed() {
        TODO("Not yet implemented")
    }

    override fun onFinishSessionConfirmed(session: Session) {
        TODO("Not yet implemented")
    }

    override fun disconnectSessionPressed() {}

    override fun onFinishAndSyncSessionConfirmed(session: Session) {}
}
