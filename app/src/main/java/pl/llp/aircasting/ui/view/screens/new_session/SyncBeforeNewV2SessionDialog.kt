package pl.llp.aircasting.ui.view.screens.new_session

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.finish_session_confirmation_dialog.view.*
import kotlinx.coroutines.launch
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseDialog
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import javax.inject.Inject

/**
 * Shown when the user starts a new mobile session and the V2 AirBeam reports a saved
 * session with unsynced measurements (HasSavedSession + hasSavedMeasurements=true).
 *
 * "Sync & Start" syncs the old session's measurements to the app DB (if the session
 * exists locally), then proceeds to start the new session.
 * "Start New Session" discards the old measurements and starts immediately.
 *
 * If the old session UUID is not found in the local DB, sync data is logged and
 * discarded — TODO: decide how to handle measurements from sessions not in the DB.
 */
class SyncBeforeNewV2SessionDialog(
    mFragmentManager: FragmentManager,
    private val onSyncAndStart: suspend () -> Unit,
    private val onJustStart: () -> Unit,
) : BaseDialog(mFragmentManager) {

    @Inject
    lateinit var v2StateRepository: AirBeamMiniV2StateRepository

    override fun setupView(inflater: LayoutInflater): View {
        val rootActivity = requireActivity()
        (rootActivity.application as AircastingApplication).userDependentComponent?.inject(this)

        val view = inflater.inflate(R.layout.finish_session_confirmation_dialog, null)

        view.header.text = getString(R.string.dialog_sync_before_new_v2_header)
        view.informations_text_view.text = getString(R.string.dialog_sync_before_new_v2_description)

        view.finish_recording_button.text = getString(R.string.sync_and_start_new_v2)
        view.finish_recording_button.setOnClickListener {
            view.finish_recording_button.isEnabled = false
            view.cancel_button.isEnabled = false
            lifecycleScope.launch {
                onSyncAndStart()
                if (isAdded) dismiss()
            }
        }

        view.cancel_button.text = getString(R.string.start_new_session_skip_sync)
        view.cancel_button.setOnClickListener {
            onJustStart()
            dismiss()
        }

        return view
    }
}
