package pl.llp.aircasting.ui.view.screens.dashboard

import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session

interface SessionCardListener {
    fun onExpandSessionCard(session: Session)
    fun onCollapseSessionCard(session: Session)
    fun onFollowButtonClicked(session: Session)
    fun onUnfollowButtonClicked(session: Session)
    fun onMapButtonClicked(session: Session, measurementStream: MeasurementStream?)
    fun onGraphButtonClicked(session: Session, measurementStream: MeasurementStream?)
}
