package pl.llp.aircasting.screens.dashboard

import pl.llp.aircasting.models.MeasurementStream
import pl.llp.aircasting.models.Session

interface SessionCardListener {
    fun onExpandSessionCard(session: Session)
    fun onFollowButtonClicked(session: Session)
    fun onUnfollowButtonClicked(session: Session)
    fun onMapButtonClicked(session: Session, measurementStream: MeasurementStream?)
    fun onGraphButtonClicked(session: Session, measurementStream: MeasurementStream?)
    fun sessionCardMoveInProgress()
}
