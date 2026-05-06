package pl.llp.aircasting.ui.view.screens.new_session

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.finish_session_confirmation_dialog.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.di.modules.IoCoroutineScope
import pl.llp.aircasting.ui.view.common.BaseDialog
import pl.llp.aircasting.ui.view.screens.common.V2WifiPickerEducationalDialog
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Shown when the user starts a new mobile session and the V2 AirBeam reports a saved
 * session with unsynced measurements (HasSavedSession + hasSavedMeasurements=true).
 *
 * The dialog drives the full sync flow itself so that the WiFi picker the orchestrator
 * spins up can return without the surrounding NewSessionActivity finishing prematurely.
 *
 * State machine:
 *   1. Initial — "Sync & Start" or "Start New Session" (skip).
 *   2. Syncing — both buttons disabled, "Syncing…" label, dialog non-cancelable.
 *   3. Success — single button "Start New Session" → [onSyncSuccess].
 *   4. Failure — single button "Continue" → [onSyncFailure] (back to dashboard, no new
 *      session, AirBeam storage left intact so the user can retry from the dashboard).
 */
class SyncBeforeNewV2SessionDialog(
    mFragmentManager: FragmentManager,
    private val onPrepareForSync: () -> Unit,
    private val onSyncSuccess: () -> Unit,
    private val onSyncFailure: () -> Unit,
    private val onSkipSync: () -> Unit,
) : BaseDialog(mFragmentManager) {

    @Inject
    lateinit var v2StateRepository: AirBeamMiniV2StateRepository

    @field:[Inject IoCoroutineScope]
    lateinit var ioScope: CoroutineScope

    private var rootView: View? = null

    override fun setupView(inflater: LayoutInflater): View {
        val rootActivity = requireActivity()
        (rootActivity.application as AircastingApplication).userDependentComponent?.inject(this)

        val view = inflater.inflate(R.layout.finish_session_confirmation_dialog, null)
        rootView = view
        renderInitial(view)
        return view
    }

    private fun renderInitial(view: View) {
        view.header.text = getString(R.string.dialog_sync_before_new_v2_header)
        view.informations_text_view.text = getString(R.string.dialog_sync_before_new_v2_description)

        view.finish_recording_button.text = getString(R.string.sync_and_start_new_v2)
        view.finish_recording_button.isEnabled = true
        view.finish_recording_button.setOnClickListener { startSync() }

        view.cancel_button.visibility = View.VISIBLE
        view.cancel_button.text = getString(R.string.start_new_session_skip_sync)
        view.cancel_button.isEnabled = true
        view.cancel_button.setOnClickListener {
            onSkipSync()
            dismiss()
        }
    }

    private fun startSync() {
        onPrepareForSync()

        rootView?.run {
            finish_recording_button.isEnabled = false
            finish_recording_button.text = getString(R.string.dialog_sync_before_new_v2_syncing)
            cancel_button.isEnabled = false
        }
        isCancelable = false

        ioScope.launch {
            val ok = runCatching {
                v2StateRepository.startSync(
                    keepConnectedAfter = true,
                    onBeforePicker = ::awaitWifiPickerEducation,
                )
            }.getOrDefault(false)
            withContext(Dispatchers.Main) {
                if (!isAdded) {
                    if (ok) onSyncSuccess() else onSyncFailure()
                    return@withContext
                }
                rootView?.let { view ->
                    if (ok) renderSuccess(view) else renderFailure(view)
                }
            }
        }
    }

    private fun renderSuccess(view: View) {
        view.header.text = getString(R.string.dialog_sync_before_new_v2_success_header)
        view.informations_text_view.text = getString(R.string.dialog_sync_before_new_v2_success_description)

        view.finish_recording_button.text = getString(R.string.dialog_sync_before_new_v2_success_button)
        view.finish_recording_button.isEnabled = true

        // Cancel button below carries the layout's bottom margin; with it gone, the
        // Continue button sits flush against the dialog's rounded edge. Mirror that
        // margin onto the visible button so the spacing matches the rest of the dialog.
        val marginPx = resources.getDimensionPixelSize(R.dimen.keyline_6)
        view.finish_recording_button?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = marginPx
        }

        view.finish_recording_button.setOnClickListener {
            onSyncSuccess()
            dismiss()
        }

        view.cancel_button.visibility = View.GONE
    }

    private suspend fun awaitWifiPickerEducation() {
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<Unit> { cont ->
                if (!isAdded) {
                    if (!cont.isCompleted) cont.resume(Unit)
                    return@suspendCancellableCoroutine
                }
                V2WifiPickerEducationalDialog(parentFragmentManager) {
                    if (!cont.isCompleted) cont.resume(Unit)
                }.show()
            }
        }
    }

    private fun renderFailure(view: View) {
        view.header.text = getString(R.string.dialog_sync_before_new_v2_failure_header)
        view.informations_text_view.text = getString(R.string.dialog_sync_before_new_v2_failure_description)

        view.finish_recording_button.text = getString(R.string.dialog_sync_before_new_v2_failure_button)
        view.finish_recording_button.isEnabled = true
        view.finish_recording_button.setOnClickListener {
            onSyncFailure()
            dismiss()
        }

        view.cancel_button.visibility = View.GONE
    }
}
