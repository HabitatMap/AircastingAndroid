package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R

abstract class ActiveSessionViewMvcImpl<ListenerType>: SessionViewMvcImpl<ListenerType> {
    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup,
        supportFragmentManager: FragmentManager
    ): super(inflater, parent, supportFragmentManager) {
    }

    override fun layoutId(): Int {
        return R.layout.active_session
    }

    override fun showMeasurementsTableValues(): Boolean {
        return true
    }
}
