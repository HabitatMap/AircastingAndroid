package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.finish_session_confirmation_dialog.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import javax.inject.Inject

/**
 * Shown when the user finishes a mobile session and the V2 AirBeam still has
 * unsynced measurements in internal storage (firmware Status reported
 * `hasSavedMeasurements=true`). The BLE Active Sync stream on characteristic `0006`
 * is already draining those measurements in the background — this dialog just
 * informs the user, blocks dismissal, and offers a single "cancel & discard"
 * escape hatch that wipes the device's storage and finishes immediately.
 *
 * Auto-finalize: observes [AirBeamMiniV2StateRepository.hasSavedMeasurementsFlow]
 * and posts the stop event the moment firmware re-notifies the flag cleared.
 */
class SyncAndFinishV2SessionDialog(
    mFragmentManager: FragmentManager,
    mSession: Session,
) : FinishSessionConfirmationDialog(mFragmentManager, mSession) {

    @Inject
    lateinit var v2StateRepository: AirBeamMiniV2StateRepository

    private var drainObserverJob: Job? = null

    override fun setupView(inflater: LayoutInflater): View {
        val view = super.setupView(inflater)
        (rootActivity.application as AircastingApplication).userDependentComponent?.inject(this)

        view.finish_recording_button.visibility = View.GONE

        view.cancel_button.text = getString(R.string.cancel_sync_and_discard)
        view.cancel_button.setOnClickListener {
            stopObservingDrain()
            onFinishMobileSessionConfirmed(mSession)
            dismiss()
        }

        // Prevent dismissal via back / outside tap — only the explicit button or the
        // auto-finalize path may close this dialog.
        isCancelable = false

        observeDrain()
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
        // drop(1) skips the StateFlow's current value (true at dialog open). We only
        // want to finalize when firmware FLIPS the flag false during this dialog's
        // lifetime — not on the initial replay.
        drainObserverJob = lifecycleScope.launch {
            v2StateRepository.hasSavedMeasurementsFlow
                .drop(1)
                .filter { !it }
                .collect {
                    if (isAdded) {
                        stopObservingDrain()
                        onFinishMobileSessionConfirmed(mSession)
                        dismiss()
                    }
                }
        }
    }

    private fun stopObservingDrain() {
        drainObserverJob?.cancel()
        drainObserverJob = null
    }

    override fun onDestroyView() {
        stopObservingDrain()
        super.onDestroyView()
    }
}
