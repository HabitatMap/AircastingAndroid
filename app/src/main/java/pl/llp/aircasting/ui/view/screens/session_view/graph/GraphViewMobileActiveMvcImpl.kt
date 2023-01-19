package pl.llp.aircasting.ui.view.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session

class GraphViewMobileActiveMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager
) : GraphViewMvcImpl(inflater, parent, supportFragmentManager) {

    override fun bindSessionMeasurementsDescription() {
        mSessionMeasurementsDescription?.text =
            context.getString(R.string.session_last_sec_measurements_description)
    }

    override fun defaultZoomSpan(): Int {
        return 1 * 60 * 1000 // 30 minutes
    }

    override fun getSessionType(): Session.Type {
        return Session.Type.MOBILE
    }
}
