package pl.llp.aircasting.ui.view.screens.session_view.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.activity_map.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.session_actions.mobile.active.MobileActiveSessionActionsBottomSheet

class MapViewMobileActiveMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager
) : MapViewMvcImpl(inflater, parent, supportFragmentManager),
    MobileActiveSessionActionsBottomSheet.Listener {
    private var mSessionActionsButton: ImageView? = null
    private var mBottomSheet: BottomSheet? = null

    init {
        mSessionActionsButton = rootView?.session_actions_button
        mSessionActionsButton?.visibility = View.VISIBLE
        mSessionActionsButton?.setOnClickListener {
            showBottomSheet(supportFragmentManager)
        }
    }

    override fun showBottomSheet(supportFragmentManager: FragmentManager) {
        mBottomSheet =
            MobileActiveSessionActionsBottomSheet(this, mSessionPresenter, supportFragmentManager)
        mBottomSheet?.show(supportFragmentManager)
    }

    override fun bindSessionMeasurementsDescription() {
        mSessionMeasurementsDescription?.text =
            context.getString(R.string.session_last_sec_measurements_description)
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

