package pl.llp.aircasting.screens.sync.syncing

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import pl.llp.aircasting.R
import pl.llp.aircasting.lib.AnimatedLoader
import pl.llp.aircasting.screens.common.BaseViewMvc
import pl.llp.aircasting.sensor.airbeam3.sync.SDCardReader
import kotlinx.android.synthetic.main.fragment_airbeam_syncing.view.*
import pl.llp.aircasting.screens.common.BaseObservableViewMvc
import pl.llp.aircasting.screens.sync.refreshed.RefreshedSessionsViewMvc

class AirbeamSyncingViewMvcImpl: BaseObservableViewMvc<AirbeamSyncingViewMvc.Listener>, AirbeamSyncingViewMvc {
    private val header: TextView?
    private val stepTitles = hashMapOf(
        SDCardReader.StepType.MOBILE to "Mobile",
        SDCardReader.StepType.FIXED_WIFI to "Fixed Wifi",
        SDCardReader.StepType.FIXED_CELLULAR to "Fixed Cellular"
    )

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): super() {
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
        AnimatedLoader(loader).start()
    }

    override fun updateProgress(step: SDCardReader.Step, linesRead: Int) {
        val title = context.getString(R.string.airbeam_syncing_header)
        val stepTitle = stepTitles[step.type]
        header?.text = "${title} ${stepTitle}: \n ${linesRead}/${step.measurementsCount}"
    }
}
