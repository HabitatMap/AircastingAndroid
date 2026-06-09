package pl.llp.aircasting.ui.view.screens.sync.confirmation

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseObservableViewMvc

class AirbeamSyncConfirmationViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    estimatedSeconds: Long
) : BaseObservableViewMvc<AirbeamSyncConfirmationViewMvc.Listener>(), AirbeamSyncConfirmationViewMvc {

    init {
        this.rootView = inflater.inflate(R.layout.fragment_airbeam_sync_confirmation, parent, false)

        val descriptionView = rootView?.findViewById<TextView>(R.id.airbeam_sync_confirmation_description)
        val etaText = formatEta(estimatedSeconds)
        descriptionView?.text = context.getString(R.string.airbeam_sync_confirmation_description, etaText)

        val yesButton = rootView?.findViewById<Button>(R.id.airbeam_sync_confirmation_yes_button)
        yesButton?.setOnClickListener {
            onYesClicked()
        }

        val noButton = rootView?.findViewById<Button>(R.id.airbeam_sync_confirmation_no_button)
        noButton?.setOnClickListener {
            onNoClicked()
        }
    }

    private fun onYesClicked() {
        for (listener in listeners) {
            listener.onYesClicked()
        }
    }

    private fun onNoClicked() {
        for (listener in listeners) {
            listener.onNoClicked()
        }
    }

    private fun formatEta(seconds: Long): String = when {
        seconds < 60L -> context.getString(R.string.sync_eta_seconds, seconds.toInt())
        seconds < 3600L -> {
            val minutes = (seconds / 60L).toInt()
            val remaining = (seconds % 60L).toInt()
            if (remaining == 0) context.getString(R.string.sync_eta_minutes, minutes)
            else context.getString(R.string.sync_eta_minutes_seconds, minutes, remaining)
        }
        else -> context.getString(
            R.string.sync_eta_hours_minutes,
            (seconds / 3600L).toInt(),
            ((seconds % 3600L) / 60L).toInt(),
        )
    }
}
