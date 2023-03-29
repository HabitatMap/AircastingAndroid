package pl.llp.aircasting.ui.view.screens.sync.syncing

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_airbeam_syncing.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseObservableViewMvc
import pl.llp.aircasting.util.extensions.startAnimation
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardReader

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

    fun finishSync() {
        for (listener in listeners) {
            listener.syncFinished()
        }
    }

    private fun startLoader() {
        val loader = rootView?.findViewById<ImageView>(R.id.loader)
        loader?.startAnimation()
    }

    override fun updateProgress(step: SDCardReader.Step, linesRead: Int) {
        val title = context.getString(R.string.airbeam_syncing_header)
        val stepTitle = stepTitles[step.type]
        header?.text = "${title} ${stepTitle}: \n ${linesRead}/${step.measurementsCount}"
        if (linesRead == step.measurementsCount) {
            header?.text = getString(R.string.airbeam_syncing_finalizing)
        }
    }
}
