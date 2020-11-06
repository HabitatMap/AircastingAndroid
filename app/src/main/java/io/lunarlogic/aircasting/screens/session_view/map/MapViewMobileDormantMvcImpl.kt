package io.lunarlogic.aircasting.screens.session_view.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R

class MapViewMobileDormantMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?
): MapDetailsViewMvcImpl(inflater, parent, supportFragmentManager) {

    override fun bindStatisticsContainer() {
        mStatisticsContainer = null
    }

    override fun bindSessionMeasurementsDescription() {
        mSessionMeasurementsDescription?.text = context.getString(R.string.session_avg_measurements_description)
    }
}
