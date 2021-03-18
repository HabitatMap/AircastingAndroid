package io.lunarlogic.aircasting.screens.session_view.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.dashboard.ActiveSessionActionsBottomSheet
import kotlinx.android.synthetic.main.activity_map.view.*

class MapViewMobileActiveMvcImpl: MapViewMvcImpl,
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

    override fun addNotePressed() {
        val session = mSessionPresenter?.session ?: return
        for (listener in listeners) {
            listener.addNoteClicked(session)
        }
    }

    override fun onFinishSessionConfirmed(session: Session) {
        val session = mSessionPresenter?.session ?: return

        for (listener in listeners) {
            listener.onFinishSessionConfirmed(session)
        }
    }

    override fun disconnectSessionPressed() {
        // do nothing
    }

    override fun onFinishAndSyncSessionConfirmed(session: Session) {
        // do nothing
    }
}

