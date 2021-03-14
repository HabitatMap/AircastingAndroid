package io.lunarlogic.aircasting.screens.sync.syncing

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.AnimatedLoader
import io.lunarlogic.aircasting.screens.common.BaseViewMvc
import io.lunarlogic.aircasting.sensor.airbeam3.sync.SDCardReader
import kotlinx.android.synthetic.main.fragment_airbeam_syncing.view.*

class AirbeamSyncingViewMvcImpl: BaseViewMvc, AirbeamSyncingViewMvc {
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

    private fun startLoader() {
        val loader = rootView?.findViewById<ImageView>(R.id.loader)
        AnimatedLoader(loader).start()
    }

    override fun updateProgress(step: SDCardReader.Step, linesRead: Int) {
        val title = context.getString(R.string.airbeam_syncing_header)
        val stepTitle = stepTitles[step.type]
        header?.text = "${title} ${stepTitle}: ${linesRead}/${step.measurementsCount}"
    }
}
