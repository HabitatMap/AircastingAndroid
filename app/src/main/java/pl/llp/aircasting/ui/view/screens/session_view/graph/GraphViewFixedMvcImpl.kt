package pl.llp.aircasting.ui.view.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.Session

open class GraphViewFixedMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?
): GraphViewMvcImpl(inflater, parent, supportFragmentManager) {

    private val MEASUREMENTS_SAMPLE_SIZE = 24 * 60 // 24h

    override fun defaultZoomSpan(): Int? {
        return 24 * 60 * 60 * 1000 // 24 hours
    }

    override fun measurementsSample(): List<Measurement> {
        return mSessionPresenter?.selectedStream?.getLastMeasurements(MEASUREMENTS_SAMPLE_SIZE) ?: listOf<Measurement>()
    }

    override fun getSessionType(): Session.Type {
        return Session.Type.FIXED
    }
}