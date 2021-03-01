package io.lunarlogic.aircasting.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R

class GraphViewMobileActiveMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?
    ): GraphViewMvcImpl(inflater, parent, supportFragmentManager) {

    override fun bindSessionMeasurementsDescription() {
        mSessionMeasurementsDescription?.text = context.getString(R.string.session_last_sec_measurements_description)
    }

    override fun defaultZoomSpan(): Int? {
        return 2 * 60 * 1000 // 30 minutes, changed to 2 minutes
    }
}
