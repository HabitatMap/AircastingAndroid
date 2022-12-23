package pl.llp.aircasting.ui.view.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.api.util.Constants
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.Session

open class GraphViewFixedMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager
) : GraphViewMvcImpl(inflater, parent, supportFragmentManager) {

    override fun defaultZoomSpan(): Int? {
        return 24 * Constants.MILLIS_IN_HOUR // 24 hours
    }

    override fun measurementsSample(): List<Measurement> {
        return mSessionPresenter?.selectedStream?.getLast24HoursOfMeasurements() ?: listOf()
    }

    override fun getSessionType(): Session.Type {
        return Session.Type.FIXED
    }
}
