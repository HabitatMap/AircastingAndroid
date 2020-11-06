package io.lunarlogic.aircasting.screens.session_view.map

import android.location.Location
import io.lunarlogic.aircasting.screens.session_view.hlu.HLUListener
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.screens.session_view.SessionViewMvc

interface MapViewMvc: ObservableViewMvc<MapViewMvc.Listener>, SessionViewMvc {
    fun centerMap(location: Location)

    interface Listener: HLUListener {
        fun locateRequested()
    }
}
