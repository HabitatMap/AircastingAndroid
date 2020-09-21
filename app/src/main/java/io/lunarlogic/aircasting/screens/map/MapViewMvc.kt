package io.lunarlogic.aircasting.screens.map

import io.lunarlogic.aircasting.screens.common.ViewMvc
import io.lunarlogic.aircasting.sensor.Session

interface MapViewMvc: ViewMvc {
    fun bindSession(session: Session)
}
