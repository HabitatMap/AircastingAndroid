package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session

class SessionPresenter(
    var session: Session,
    var expanded: Boolean = false,
    var loading: Boolean = false,
    var selectedStream: MeasurementStream? = null
) {
    fun setDefaultStream() {
        selectedStream = session.streamsSortedByDetailedType().firstOrNull()
    }
}
