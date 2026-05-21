package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.finish_session_confirmation_dialog.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.modules.IoCoroutineScope
import pl.llp.aircasting.ui.view.screens.common.V2WifiPickerEducationalDialog
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2.V2BleSyncOrchestrator
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Shown after [FinishSessionConfirmationDialog] when the user finishes a mobile session
 * and the V2 AirBeam still has unsynced measurements in internal storage. Drives the
 * same BLE manual-sync flow as [pl.llp.aircasting.ui.view.screens.new_session.SyncBeforeNewV2SessionDialog]
 * so the device's stored buffer is drained over BLE (firmware stops the running session
 * in its `StartBleSync` handler — no new mobile measurements are streamed after this).
 *
 * The user can opt out via the "Finish Without Syncing" button, which falls through to
 * the standard discard path (`StopRecordingEvent` → `AirBeamConnector.discardSession()`).
 */
class SyncAndFinishV2SessionDialog(
    mFragmentManager: FragmentManager,
    mSession: Session,
) : FinishSessionConfirmationDialog(mFragmentManager, mSession) {

    @Inject
    lateinit var v2StateRepository: AirBeamMiniV2StateRepository

    @field:[Inject IoCoroutineScope]
    lateinit var ioScope: CoroutineScope

    private var rootView: View? = null
    private var progressJob: Job? = null

    override fun setupView(inflater: LayoutInflater): View {
        val view = super.setupView(inflater)
        (rootActivity.application as AircastingApplication).userDependentComponent?.inject(this)
        rootView = view
        renderInitial(view)
        return view
    }

    private fun renderInitial(view: View) {
        view.header.text = getString(R.string.dialog_sync_and_finish_v2_header)
        view.informations_text_view.text = buildInitialDescription()

        view.finish_recording_button.text = getString(R.string.sync_and_finish_v2)
        view.finish_recording_button.isEnabled = true
        view.finish_recording_button.setOnClickListener { startSync() }

        view.cancel_button.visibility = View.VISIBLE
        view.cancel_button.text = getString(R.string.finish_v2_skip_sync)
        view.cancel_button.isEnabled = true
        view.cancel_button.setOnClickListener {
            finishWithoutSync()
        }
    }

    private fun startSync() {
        rootView?.run {
            finish_recording_button.isEnabled = false
            finish_recording_button.text = syncingButtonText(0)
            cancel_button.isEnabled = false
        }
        isCancelable = false
        observeSyncProgress()

        ioScope.launch {
            val ok = runCatching {
                v2StateRepository.startSync(
                    keepConnectedAfter = true,
                    onBeforePicker = ::awaitWifiPickerEducation,
                )
            }.getOrDefault(false)
            withContext(Dispatchers.Main) {
                progressJob?.cancel()
                if (!isAdded) {
                    finishSession()
                    return@withContext
                }
                rootView?.let { view ->
                    if (ok) renderSuccess(view) else renderFailure(view)
                }
            }
        }
    }

    private fun renderSuccess(view: View) {
        view.header.text = getString(R.string.dialog_sync_and_finish_v2_success_header)
        view.informations_text_view.text = getString(R.string.dialog_sync_and_finish_v2_success_description)

        view.finish_recording_button.text = getString(R.string.dialog_sync_and_finish_v2_success_button)
        view.finish_recording_button.isEnabled = true

        val marginPx = resources.getDimensionPixelSize(R.dimen.keyline_6)
        view.finish_recording_button?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = marginPx
        }

        view.finish_recording_button.setOnClickListener { finishSession() }
        view.cancel_button.visibility = View.GONE
    }

    private fun renderFailure(view: View) {
        view.header.text = getString(R.string.dialog_sync_and_finish_v2_failure_header)
        view.informations_text_view.text = getString(R.string.dialog_sync_and_finish_v2_failure_description)

        view.finish_recording_button.text = getString(R.string.dialog_sync_and_finish_v2_failure_button)
        view.finish_recording_button.isEnabled = true

        val marginPx = resources.getDimensionPixelSize(R.dimen.keyline_6)
        view.finish_recording_button?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = marginPx
        }

        view.finish_recording_button.setOnClickListener { finishSession() }
        view.cancel_button.visibility = View.GONE
    }

    private fun finishWithoutSync() {
        progressJob?.cancel()
        finishSession()
    }

    private fun finishSession() {
        onFinishMobileSessionConfirmed(mSession)
        dismiss()
    }

    /**
     * Mirror the orchestrator's progress on the action button. Scoped to the dialog so it
     * cancels on dismissal; the underlying sync runs in [ioScope] (UserSessionScope) and is
     * unaffected.
     */
    private fun observeSyncProgress() {
        progressJob?.cancel()
        progressJob = lifecycleScope.launch {
            v2StateRepository.syncProgress.collect { percent ->
                rootView?.finish_recording_button?.text = syncingButtonText(percent)
            }
        }
    }

    private fun syncingButtonText(percent: Int): String =
        getString(R.string.dialog_sync_before_new_v2_syncing_with_percent, percent)

    private fun buildInitialDescription(): CharSequence {
        val seconds = V2BleSyncOrchestrator.estimateSyncSeconds(v2StateRepository.savedSessionFileSize)
        if (seconds <= 0L) return getString(R.string.dialog_sync_and_finish_v2_description)
        val html = getString(R.string.dialog_sync_and_finish_v2_description_with_eta, formatEta(seconds))
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun formatEta(seconds: Long): String = when {
        seconds < 60L -> getString(R.string.sync_eta_seconds, seconds.toInt())
        seconds < 3600L -> {
            val minutes = (seconds / 60L).toInt()
            val remaining = (seconds % 60L).toInt()
            if (remaining == 0) getString(R.string.sync_eta_minutes, minutes)
            else getString(R.string.sync_eta_minutes_seconds, minutes, remaining)
        }
        else -> getString(
            R.string.sync_eta_hours_minutes,
            (seconds / 3600L).toInt(),
            ((seconds % 3600L) / 60L).toInt(),
        )
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

    override fun onDestroyView() {
        progressJob?.cancel()
        super.onDestroyView()
    }
}
