package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.mobile.active

import android.view.View
import kotlinx.android.synthetic.main.active_session_actions.view.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardPagerAdapter.Companion.MOBILE_ACTIVE_TAB_INDEX
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.dashboard.active.AddNoteBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.active.FinishSessionConfirmationDialog
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.ui.view.screens.sync.SyncUnavailableDialog
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector
import pl.llp.aircasting.util.isSDKLessOrEqualToNMR1
import javax.inject.Inject

open class MobileActiveSessionActionsBottomSheet(
    private val mSessionPresenter: SessionPresenter?
) : BottomSheet() {
    constructor() : this(null)

    @Inject
    lateinit var errorHandler: ErrorHandler

    @Inject
    lateinit var permissionsManager: PermissionsManager

    @Inject
    lateinit var airBeamReconnector: AirBeamReconnector

    override fun layoutId(): Int {
        return R.layout.active_session_actions
    }

    override fun setup() {
        (requireActivity().application as AircastingApplication).appComponent.inject(this)
        setupDisconnectedButton()
        setupStopButton()
        setupAddNoteButton()
        setupCancelButton()
    }

    private fun setupDisconnectedButton() {
        val session = mSessionPresenter?.session ?: return
        val disconnectButton = contentView?.disconnect_session_button

        if (mSessionPresenter.isDisconnectable()) {
            disconnectButton?.setOnClickListener {
                if (isSDKLessOrEqualToNMR1()) {
                    SyncUnavailableDialog(parentFragmentManager)
                        .show()
                } else {
                    airBeamReconnector.disconnect(session)
                }
                MainActivity.navigate(context, MOBILE_ACTIVE_TAB_INDEX)
                dismiss()
            }
        } else {
            disconnectButton?.visibility = View.GONE
        }
    }

    private fun setupStopButton() {
        val stopButton = contentView?.stop_session_button
        val session = mSessionPresenter?.session ?: return
        stopButton?.setOnClickListener {
            FinishSessionConfirmationDialog(parentFragmentManager, session).show()
            dismiss()
        }
    }

    private fun setupAddNoteButton() {
        val addNoteButton = contentView?.add_note_button
        val session = mSessionPresenter?.session ?: return
        addNoteButton?.setOnClickListener {
            AddNoteBottomSheet(session, requireActivity(), errorHandler, permissionsManager).show(
                requireActivity().supportFragmentManager
            )
            dismiss()
        }
    }

    private fun setupCancelButton() {
        val cancelButton = contentView?.cancel_button
        cancelButton?.setOnClickListener {
            dismiss()
        }
    }
}
