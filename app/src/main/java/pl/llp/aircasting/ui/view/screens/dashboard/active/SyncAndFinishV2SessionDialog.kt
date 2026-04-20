package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.finish_session_confirmation_dialog.view.*
import kotlinx.coroutines.launch
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import javax.inject.Inject

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

    override fun setupView(inflater: LayoutInflater): View {
        val view = super.setupView(inflater)
        (rootActivity.application as AircastingApplication).userDependentComponent?.inject(this)

        view.cancel_button.text = getString(R.string.finish_without_sync)
        view.cancel_button.setOnClickListener {
            onFinishMobileSessionConfirmed(mSession)
            dismiss()
        }

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
        lifecycleScope.launch {
            v2StateRepository.startSync()
            onFinishMobileSessionConfirmed(mSession)
            if (isAdded) dismiss()
        }
    }
}
