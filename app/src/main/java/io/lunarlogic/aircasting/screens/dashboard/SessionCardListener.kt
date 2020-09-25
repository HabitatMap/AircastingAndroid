package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session

interface SessionCardListener {
    fun onExpandSessionCard(session: Session)
    fun onMapButtonClicked(session: Session, measurementStream: MeasurementStream?)
}
