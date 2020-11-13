package io.lunarlogic.aircasting.screens.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R

class MapViewFixedMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?): MapViewMvcImpl(inflater, parent, supportFragmentManager), MapViewMvc, MapViewMvc.HLUDialogListener {

    override fun bindSessionMeasurementsDescription() {
        mSessionMeasurementsDescription?.text = context.getString(R.string.session_measurements_description)
    }
}
