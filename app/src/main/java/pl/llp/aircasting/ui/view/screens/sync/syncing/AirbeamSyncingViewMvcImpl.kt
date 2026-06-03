package pl.llp.aircasting.ui.view.screens.sync.syncing

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_airbeam_syncing.view.airbeam_syncing_header
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseObservableViewMvc
import pl.llp.aircasting.util.extensions.startAnimation
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardReader

class AirbeamSyncingViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<AirbeamSyncingViewMvc.Listener>(), AirbeamSyncingViewMvc {
    private val header: TextView?
    private val stepTitles = hashMapOf(
        SDCardReader.StepType.MOBILE to "Mobile",
        SDCardReader.StepType.FIXED_WIFI to "Fixed Wifi",
        SDCardReader.StepType.FIXED_CELLULAR to "Fixed Cellular"
    )

    init {
        this.rootView = inflater.inflate(R.layout.fragment_airbeam_syncing, parent, false)
        header = rootView?.airbeam_syncing_header
        val title = context.getString(R.string.airbeam_syncing_header)
        header?.text = "${title}..."
        startLoader()
    }

    fun finishSync(isV2: Boolean) {
        for (listener in listeners) {
            listener.syncFinished(isV2)
        }
    }

    private fun startLoader() {
        val loader = rootView?.findViewById<ImageView>(R.id.loader)
        loader?.startAnimation()
    }

    override fun updateProgress(step: SDCardReader.Step, linesRead: Int) {
        val title = context.getString(R.string.airbeam_syncing_header)
        val stepTitle = stepTitles[step.type]
        header?.text = "${title} ${stepTitle}: ${linesRead}/${step.measurementsCount}"
        if (linesRead == step.measurementsCount) {
            header?.text = getString(R.string.airbeam_syncing_finalizing)
        }
    }

    override fun updateV2Progress(percent: Int) {
        val title = context.getString(R.string.airbeam_syncing_header)
        header?.text = if (percent >= 100) {
            getString(R.string.airbeam_syncing_finalizing)
        } else {
            "$title… $percent%"
        }
    }

    override fun displayEstimatedTime(seconds: Long) {
        val descriptionView = rootView?.findViewById<TextView>(R.id.airbeam_syncing_description)
        val defaultDescription = context.getString(R.string.airbeam_syncing_description)
        val etaText = formatEta(seconds)
        val estimatedTimeTemplate = context.getString(R.string.airbeam_syncing_estimated_time, etaText)
        descriptionView?.text = "$defaultDescription\n\n$estimatedTimeTemplate"
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
