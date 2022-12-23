package pl.llp.aircasting.ui.view.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session

class GraphViewMobileDormantMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager
): GraphViewMvcImpl(inflater, parent, supportFragmentManager) {

    override fun bindSessionMeasurementsDescription() {
        mSessionMeasurementsDescription?.text = context.getString(R.string.session_avg_measurements_description)
    }

    override fun defaultZoomSpan(): Int? {
        return null // will fallback to entire session span
    }

    override fun shouldShowStatisticsContainer(): Boolean {
        return false
    }

    override fun getSessionType(): Session.Type {
        return Session.Type.MOBILE
    }
}
