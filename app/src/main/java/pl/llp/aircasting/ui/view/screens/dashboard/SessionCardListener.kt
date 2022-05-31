package pl.llp.aircasting.ui.view.screens.dashboard

import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.LocalSession

interface SessionCardListener {
    fun onExpandSessionCard(localSession: LocalSession)
    fun onFollowButtonClicked(localSession: LocalSession)
    fun onUnfollowButtonClicked(localSession: LocalSession)
    fun onMapButtonClicked(localSession: LocalSession, measurementStream: MeasurementStream?)
    fun onGraphButtonClicked(localSession: LocalSession, measurementStream: MeasurementStream?)
}
