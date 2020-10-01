package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session

class SessionPresenter(
    var session: Session,
    var selectedStream: MeasurementStream? = null,
    var expanded: Boolean = false,
    var loading: Boolean = false
) {

    init {
        if (selectedStream == null) {
            setDefaultStream()
        }
    }

    fun setDefaultStream() {
        selectedStream = defaultStream(session)
    }

    companion object {
        fun defaultStream(session: Session): MeasurementStream? {
            return session.streamsSortedByDetailedType().firstOrNull()
        }
    }
}
