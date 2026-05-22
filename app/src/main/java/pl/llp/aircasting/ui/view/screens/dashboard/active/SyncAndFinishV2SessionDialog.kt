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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.modules.IoCoroutineScope
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2.V2BleSyncOrchestrator
import javax.inject.Inject

/**
 * Shown after [FinishSessionConfirmationDialog] when the user finishes a mobile session
 * and the V2 AirBeam still has unsynced measurements in storage.
 *
 * Plan (c): the dialog immediately writes `StartBleSync (0x16)` on open, so
 * `Status::ReadyToSync (0x03)` lands with the firmware-reported `file_size_u64_LE`
 * (FW commit `ed751b180`) within ~100 ms. That value drives both the ETA hint in the
 * description and the 0–100% progress label on the action button.
 *
 * Three exit paths converge on [onFinishMobileSessionConfirmed]:
 * 1. **Discard mid-stream** — user taps "Discard & Finish". Cancels the orchestrator
 *    coroutine (drops collected records, no DB insert) and posts `StopRecordingEvent`,
 *    which routes through `AirBeamConnector.discardSession()` to write `0x11
 *    DiscardSession` and disconnect. Relies on firmware honoring `0x11` while
 *    `StartBleSync` is still streaming.
 * 2. **Sync success** — orchestrator inserts records, marks session FINISHED, dialog
 *    renders "Sync complete". User taps Done → standard `StopRecordingEvent` cleanup
 *    (FW storage already auto-cleared via Ready 0x22 so the trailing `0x11` is a no-op).
 * 3. **Sync failure** (Nack 0x06 / write failure / timeout) — dialog renders "Sync
 *    failed". User taps Continue → same cleanup as discard; on-device data may still
 *    be present but `StopRecordingEvent` wipes it via `0x11`.
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
    private var etaJob: Job? = null
    private var syncJob: Job? = null
    private var finalized = false

    override fun setupView(inflater: LayoutInflater): View {
        val view = super.setupView(inflater)
        (rootActivity.application as AircastingApplication).userDependentComponent?.inject(this)
        rootView = view
        renderSyncing(view, etaSeconds = null)
        startSync()
        return view
    }

    private fun startSync() {
        isCancelable = false
        observeProgress()
        observeFileSize()
        syncJob = ioScope.launch {
            val ok = runCatching {
                v2StateRepository.startSync(keepConnectedAfter = true)
            }.getOrDefault(false)
            withContext(Dispatchers.Main) {
                progressJob?.cancel()
                etaJob?.cancel()
                if (finalized) return@withContext
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

    private fun renderSyncing(view: View, etaSeconds: Long?) {
        view.header.text = getString(R.string.dialog_sync_and_finish_v2_header)
        view.informations_text_view.text = buildSyncingDescription(etaSeconds)

        view.finish_recording_button.text = getString(R.string.dialog_sync_and_finish_v2_preparing)
        view.finish_recording_button.isEnabled = false
        view.finish_recording_button.setOnClickListener(null)

        view.cancel_button.visibility = View.VISIBLE
        view.cancel_button.text = getString(R.string.finish_v2_skip_sync)
        view.cancel_button.isEnabled = true
        view.cancel_button.setOnClickListener { discardAndFinish() }
    }

    private fun renderSuccess(view: View) {
        view.header.text = getString(R.string.dialog_sync_and_finish_v2_success_header)
        view.informations_text_view.text = getString(R.string.dialog_sync_and_finish_v2_success_description)

        view.finish_recording_button.text = getString(R.string.dialog_sync_and_finish_v2_success_button)
        view.finish_recording_button.isEnabled = true
        applyBottomMarginToPrimary(view)
        view.finish_recording_button.setOnClickListener { finishSession() }

        view.cancel_button.visibility = View.GONE
    }

    private fun renderFailure(view: View) {
        view.header.text = getString(R.string.dialog_sync_and_finish_v2_failure_header)
        view.informations_text_view.text = getString(R.string.dialog_sync_and_finish_v2_failure_description)

        view.finish_recording_button.text = getString(R.string.dialog_sync_and_finish_v2_failure_button)
        view.finish_recording_button.isEnabled = true
        applyBottomMarginToPrimary(view)
        view.finish_recording_button.setOnClickListener { finishSession() }

        view.cancel_button.visibility = View.GONE
    }

    private fun applyBottomMarginToPrimary(view: View) {
        val marginPx = resources.getDimensionPixelSize(R.dimen.keyline_6)
        view.finish_recording_button?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = marginPx
        }
    }

    private fun buildSyncingDescription(etaSeconds: Long?): CharSequence {
        if (etaSeconds == null || etaSeconds <= 0L) {
            return getString(R.string.dialog_sync_and_finish_v2_description_syncing)
        }
        val html = getString(R.string.dialog_sync_and_finish_v2_description_with_eta, formatEta(etaSeconds))
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun observeFileSize() {
        etaJob?.cancel()
        etaJob = lifecycleScope.launch {
            val fileSize = v2StateRepository.readyToSyncFileSize.first()
            val seconds = V2BleSyncOrchestrator.estimateSyncSeconds(fileSize)
            rootView?.informations_text_view?.text = buildSyncingDescription(seconds)
        }
    }

    private fun observeProgress() {
        progressJob?.cancel()
        progressJob = lifecycleScope.launch {
            v2StateRepository.syncProgress.collect { percent ->
                rootView?.finish_recording_button?.text =
                    getString(R.string.dialog_sync_before_new_v2_syncing_with_percent, percent)
            }
        }
    }

    private fun discardAndFinish() {
        if (finalized) return
        finalized = true
        progressJob?.cancel()
        etaJob?.cancel()
        syncJob?.cancel()
        // StopRecordingEvent path in AirBeamConnector writes `0x11 DiscardSession`
        // before disconnecting, which interrupts the in-flight `StartBleSync` stream
        // and wipes the on-device storage. Coroutine cancel above unwinds the
        // orchestrator's finally block (clears chunk handler, setSyncInProgress(false))
        // so no collected records are inserted into the local DB.
        onFinishMobileSessionConfirmed(mSession)
        dismiss()
    }

    private fun finishSession() {
        if (finalized) return
        finalized = true
        onFinishMobileSessionConfirmed(mSession)
        dismiss()
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

    override fun onDestroyView() {
        progressJob?.cancel()
        etaJob?.cancel()
        super.onDestroyView()
    }
}
