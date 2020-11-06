package io.lunarlogic.aircasting.screens.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.HLUDialogListener

class MapViewMobileActiveMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?): MapViewMvcImpl(inflater, parent, supportFragmentManager), MapViewMvc,
    HLUDialogListener {

    override fun bindSessionMeasurementsDescription() {
        mSessionMeasurementsDescription?.text = context.getString(R.string.session_last_sec_measurements_description)
    }
}

