package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.finish_session_confirmation_dialog.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.modules.IoCoroutineScope
import pl.llp.aircasting.ui.view.screens.common.V2WifiPickerEducationalDialog
import pl.llp.aircasting.util.events.StopRecordingEvent
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Shown when the user finishes a mobile session and the V2 AirBeam has measurements
 * that haven't been streamed to the phone yet (device reported hasSavedMeasurements=true
 * while in Running state). Offers "Sync & Finish" (sends StartSync, waits, then stops)
 * or "Finish Without Sync" (stops immediately, discarding unsynced data).
 */
class SyncAndFinishV2SessionDialog(
    mFragmentManager: FragmentManager,
    mSession: Session,
) : FinishSessionConfirmationDialog(mFragmentManager, mSession) {

    @Inject
    lateinit var v2StateRepository: AirBeamMiniV2StateRepository

    @field:[Inject IoCoroutineScope]
    lateinit var ioScope: CoroutineScope

    private var dialogView: View? = null
    private var progressJob: Job? = null

    override fun setupView(inflater: LayoutInflater): View {
        val view = super.setupView(inflater)
        (rootActivity.application as AircastingApplication).userDependentComponent?.inject(this)

        view.cancel_button.text = getString(R.string.finish_without_sync)
        view.cancel_button.setOnClickListener {
            onFinishMobileSessionConfirmed(mSession)
            dismiss()
        }

        dialogView = view
        return view
    }

    override fun buildHeader(): SpannableStringBuilder =
        SpannableStringBuilder()
            .append(getString(R.string.dialog_sync_and_finish_v2_header))
            .append(" ")
            .color(blueColor()) { bold { append(mSession.name) } }
            .append("?")

    override fun buildDescription(): SpannableStringBuilder =
        SpannableStringBuilder().append(getString(R.string.dialog_sync_and_finish_v2_description))

    override fun finishButtonText() = getString(R.string.sync_and_finish_v2)

    override fun finishSessionConfirmed() {
        dialogView?.run {
            finish_recording_button.isEnabled = false
            finish_recording_button.text = syncingButtonText(0)
            cancel_button.isEnabled = false
        }
        isCancelable = false
        observeSyncProgress()

        val session = mSession
        // Run the sync on a UserSessionScope so dialog dismissal / activity navigation can't
        // cancel it mid-flight. The orchestrator must finish three things in order or the
        // session ends up in a half-state: (1) HTTP /sync download + measurement insert,
        // (2) BLE reconnect + DiscardSession to clear AirBeam storage, (3) FINISHED transition
        // and backend upload. V2MobileMeasurementsInserter skips finished sessions, so the
        // FINISHED transition has to come AFTER the orchestrator returns.
        ioScope.launch {
            v2StateRepository.startSync(onBeforePicker = ::awaitWifiPickerEducation)
            // StopRecordingEvent → SessionManager → RecordingHandler.stopRecording marks the
            // session FINISHED and runs sessionsSyncService.sync() to upload it to the
            // backend. AirBeam storage Discard already happened inside the orchestrator
            // (V2SyncOrchestrator.reconnectAndSendDiscard).
            withContext(Dispatchers.Main) {
                if (isAdded) {
                    onFinishMobileSessionConfirmed(session)
                    dismiss()
                } else {
                    // Activity/dialog already gone — fall back to direct event post + count
                    // bookkeeping so the session still finalizes locally and uploads.
                    EventBus.getDefault().post(StopRecordingEvent(session.uuid))
                    settings.decreaseActiveMobileSessionsCount()
                }
            }
        }
    }

    /**
     * Mirror the orchestrator's WiFi-download progress on the action button so the user
     * gets the same 0..100% feedback as the SD-sync wizard. Uses `lifecycleScope` so
     * the collector is cancelled when the dialog goes away — but the underlying sync
     * keeps running in [ioScope] (UserSessionScope) so dismissal doesn't cancel it.
     */
    private fun observeSyncProgress() {
        progressJob?.cancel()
        progressJob = lifecycleScope.launch {
            v2StateRepository.syncProgress.collect { percent ->
                dialogView?.finish_recording_button?.text = syncingButtonText(percent)
            }
        }
    }

    private fun syncingButtonText(percent: Int): String =
        getString(R.string.dialog_sync_and_finish_v2_syncing, percent)

    override fun onDestroyView() {
        progressJob?.cancel()
        super.onDestroyView()
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
}
