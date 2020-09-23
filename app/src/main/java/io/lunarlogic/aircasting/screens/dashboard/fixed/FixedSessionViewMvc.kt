package io.lunarlogic.aircasting.screens.dashboard.fixed

import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvc
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session

interface FixedSessionViewMvc:
    SessionViewMvc<FixedSessionViewMvc.Listener> {
    interface Listener {
        fun onSessionDeleteClicked(session: Session)
        fun onMapButtonClicked(session: Session, measurementStream: MeasurementStream?)
    }
}
