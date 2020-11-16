package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.Session

interface SessionCardListener {
    fun onExpandSessionCard(session: Session)
    fun onFollowButtonClicked(session: Session)
    fun onUnfollowButtonClicked(session: Session)
    fun onMapButtonClicked(session: Session, measurementStream: MeasurementStream?)
    fun onGraphButtonClicked(session: Session, measurementStream: MeasurementStream?)
}
