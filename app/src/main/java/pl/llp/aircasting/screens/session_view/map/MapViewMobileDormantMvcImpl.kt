package pl.llp.aircasting.screens.session_view.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.models.Session

class MapViewMobileDormantMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?
): MapViewMvcImpl(inflater, parent, supportFragmentManager) {

    override fun bindSessionMeasurementsDescription() {
        mSessionMeasurementsDescription?.text = context.getString(R.string.session_avg_measurements_description_long)
    }

    override fun shouldShowStatisticsContainer(): Boolean {
        return false
    }

    override fun getSessionType(): Session.Type {
        return Session.Type.MOBILE
    }
}
