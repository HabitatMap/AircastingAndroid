package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.sensor.SensorThreshold
import io.lunarlogic.aircasting.sensor.Session


interface SessionsViewMvc : ObservableViewMvc<SessionsViewMvc.Listener> {

    interface Listener {
        fun onRecordNewSessionClicked()
        fun onSwipeToRefreshTriggered(callback: () -> Unit)
        fun onStopSessionClicked(sessionUUID: String)
        fun onDeleteSessionClicked(sessionUUID: String)
        fun onMapButtonClicked(sessionUUID: String, sensorName: String?)
        fun onExpandSessionCard(session: Session)
    }

    fun showSessionsView(sessions: List<Session>, sensorThresholds: HashMap<String, SensorThreshold>)
    fun showEmptyView()
    fun showLoaderFor(session: Session)
    fun hideLoaderFor(session: Session)
}
