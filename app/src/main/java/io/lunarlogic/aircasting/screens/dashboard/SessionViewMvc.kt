package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.sensor.Session

interface SessionViewMvc<ListenerType>: ObservableViewMvc<ListenerType> {
    fun bindSession(session: Session)
}