package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.finish_session_confirmation_dialog.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2.V2BleSyncOrchestrator
import javax.inject.Inject

/**
 * Shown when the user finishes a mobile session and the V2 AirBeam still has
 * unsynced measurements in internal storage (firmware Status reported
 * `hasSavedMeasurements=true`) OR is currently mid-drain on the Active Sync
 * (`0006`) characteristic. The BLE Active Sync stream is already flushing those
 * measurements in the background — this dialog just informs the user, blocks
 * dismissal, and offers a single "cancel & discard" escape hatch that wipes the
 * device's storage and finishes immediately.
 *
 * Auto-finalize: observes the OR of
 * [AirBeamMiniV2StateRepository.hasSavedMeasurementsFlow] and
 * [AirBeamMiniV2StateRepository.activeSyncDrainingFlow] and posts the stop
 * event once both flip false (drain complete).
 *
 * Countdown ETA: when firmware reported a non-zero `savedSessionFileSize` (FW
 * commit `3990cf22`), the description shows a per-second countdown derived
 * from [V2BleSyncOrchestrator.estimateSyncSeconds] minus elapsed since the
 * first `0006` chunk of this drain.
 */
class SyncAndFinishV2SessionDialog(
    mFragmentManager: FragmentManager,
    mSession: Session,
) : FinishSessionConfirmationDialog(mFragmentManager, mSession) {

    @Inject
    lateinit var v2StateRepository: AirBeamMiniV2StateRepository

    private var drainObserverJob: Job? = null
    private var etaTickerJob: Job? = null
    private var dialogView: View? = null

    private var totalEstimateSeconds: Long = 0L
    private var fallbackStartMs: Long = 0L

    override fun setupView(inflater: LayoutInflater): View {
        val view = super.setupView(inflater)
        (rootActivity.application as AircastingApplication).userDependentComponent?.inject(this)

        dialogView = view
        view.finish_recording_button.visibility = View.GONE

        view.cancel_button.text = getString(R.string.cancel_sync_and_discard)
        view.cancel_button.setOnClickListener {
            stopObservers()
            onFinishMobileSessionConfirmed(mSession)
            dismiss()
        }

        isCancelable = false

        totalEstimateSeconds =
            V2BleSyncOrchestrator.estimateSyncSeconds(v2StateRepository.savedSessionFileSize)
        fallbackStartMs = System.currentTimeMillis()

        observeDrain()
        if (totalEstimateSeconds > 0L) startEtaTicker(view)
        return view
    }

    override fun buildHeader(): SpannableStringBuilder =
        SpannableStringBuilder()
            .append(getString(R.string.dialog_finish_with_active_sync_header))
            .append(" ")
            .color(blueColor()) { bold { append(mSession.name) } }

    override fun buildDescription(): SpannableStringBuilder =
        SpannableStringBuilder().append(getString(R.string.dialog_finish_with_active_sync_description))

    private fun observeDrain() {
        drainObserverJob?.cancel()
        // drop(1) skips the combined StateFlow's current value (true at dialog open).
        // We only want to finalize when both signals FLIP false during this dialog's
        // lifetime — not on the initial replay.
        drainObserverJob = lifecycleScope.launch {
            combine(
                v2StateRepository.hasSavedMeasurementsFlow,
                v2StateRepository.activeSyncDrainingFlow,
            ) { hasSaved, draining -> hasSaved || draining }
                .drop(1)
                .filter { !it }
                .collect {
                    if (isAdded) {
                        stopObservers()
                        onFinishMobileSessionConfirmed(mSession)
                        dismiss()
                    }
                }
        }
    }

    private fun startEtaTicker(view: View) {
        etaTickerJob?.cancel()
        etaTickerJob = lifecycleScope.launch {
            while (isActive) {
                renderEta(view)
                delay(1_000L)
            }
        }
    }

    private fun renderEta(view: View) {
        val startMs = v2StateRepository.activeSyncStartMs.takeIf { it > 0L } ?: fallbackStartMs
        val elapsedSec = (System.currentTimeMillis() - startMs) / 1_000L
        val remaining = (totalEstimateSeconds - elapsedSec).coerceAtLeast(0L)
        val html = getString(
            R.string.dialog_finish_with_active_sync_description_with_eta,
            formatEta(remaining),
        )
        view.informations_text_view.text =
            HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
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

    private fun stopObservers() {
        drainObserverJob?.cancel()
        drainObserverJob = null
        etaTickerJob?.cancel()
        etaTickerJob = null
    }

    override fun onDestroyView() {
        stopObservers()
        dialogView = null
        super.onDestroyView()
    }
}
