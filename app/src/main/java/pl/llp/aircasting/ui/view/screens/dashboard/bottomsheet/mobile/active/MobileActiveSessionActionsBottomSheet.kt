package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.mobile.active

import kotlinx.android.synthetic.main.active_session_actions.view.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.dashboard.active.AddNoteBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.active.FinishSessionConfirmationDialog
import pl.llp.aircasting.ui.view.screens.dashboard.active.SyncAndFinishV2SessionDialog
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import pl.llp.aircasting.util.helpers.sensor.common.connector.AirBeamReconnector
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

    @Inject
    lateinit var v2StateRepository: AirBeamMiniV2StateRepository

    override fun layoutId(): Int {
        return R.layout.active_session_actions
    }

    override fun setup() {
        (requireActivity().application as AircastingApplication).userDependentComponent?.inject(this)
        setupStopButton()
        setupAddNoteButton()
        setupCancelButton()
    }

    private fun setupStopButton() {
        val stopButton = contentView?.stop_session_button
        val session = mSessionPresenter?.session ?: return
        stopButton?.setOnClickListener {
            val needsV2Sync = v2StateRepository.hasSavedMeasurements || v2StateRepository.isActiveSyncDraining
            val onConfirmed: (() -> Unit)? = if (needsV2Sync) {
                { SyncAndFinishV2SessionDialog(parentFragmentManager, session).show() }
            } else null
            FinishSessionConfirmationDialog(parentFragmentManager, session, onConfirmed).show()
            dismiss()
        }
    }

    private fun setupAddNoteButton() {
        val addNoteButton = contentView?.add_note_button
        val session = mSessionPresenter?.session ?: return
        addNoteButton?.setOnClickListener {
            AddNoteBottomSheet(session.uuid)
                .show(requireActivity().supportFragmentManager)

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
