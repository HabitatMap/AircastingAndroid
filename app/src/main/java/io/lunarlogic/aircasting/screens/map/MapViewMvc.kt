package io.lunarlogic.aircasting.screens.map

import android.location.Location
import io.lunarlogic.aircasting.screens.common.hlu.HLUListener
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.screens.common.SessionViewMvc

interface MapViewMvc: ObservableViewMvc<MapViewMvc.Listener>, SessionViewMvc {
    fun centerMap(location: Location)

    interface Listener: HLUListener {
        fun locateRequested()
    }
}
