package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.models.SensorThreshold
import io.lunarlogic.aircasting.models.Session


interface SessionsViewMvc : ObservableViewMvc<SessionsViewMvc.Listener> {

    interface Listener {
        fun onRecordNewSessionClicked()
        fun onSwipeToRefreshTriggered()
        fun onStopSessionClicked(sessionUUID: String)
        fun onDeleteSessionClicked(sessionUUID: String)
        fun onFollowButtonClicked(session: Session)
        fun onUnfollowButtonClicked(session: Session)
        fun onMapButtonClicked(session: Session, sensorName: String?)
        fun onGraphButtonClicked(sessionUUID: String, sensorName: String?)
        fun onExpandSessionCard(session: Session)
    }

    fun showSessionsView(sessions: List<Session>, sensorThresholds: HashMap<String, SensorThreshold>)
    fun showEmptyView()
    fun showLoaderFor(session: Session)
    fun hideLoaderFor(session: Session)
    fun showLoader()
    fun hideLoader()
}
