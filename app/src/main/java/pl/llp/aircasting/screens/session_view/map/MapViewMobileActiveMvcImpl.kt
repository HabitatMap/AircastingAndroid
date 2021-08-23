package pl.llp.aircasting.screens.session_view.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.common.BottomSheet
import pl.llp.aircasting.screens.dashboard.ActiveSessionActionsBottomSheet
import kotlinx.android.synthetic.main.activity_map.view.*

class MapViewMobileActiveMvcImpl: MapViewMvcImpl,
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
            if (supportFragmentManager != null) { // todo: this null check to be changed <??>
                buildBottomSheet(supportFragmentManager)
            }
        }
    }

    fun buildBottomSheet(supportFragmentManager: FragmentManager) {
        mBottomSheet = ActiveSessionActionsBottomSheet(this, mSessionPresenter, supportFragmentManager)
        mBottomSheet?.show(supportFragmentManager)
    }

    fun dismissBottomSheet() {
        mBottomSheet?.dismiss()
    }

    override fun bindSessionMeasurementsDescription() {
        mSessionMeasurementsDescription?.text = context.getString(R.string.session_last_sec_measurements_description)
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

