package pl.llp.aircasting.ui.view.screens.dashboard

import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.common.ObservableViewMvc
import pl.llp.aircasting.ui.view.screens.dashboard.active.FinishSessionListener


interface SessionsViewMvc : ObservableViewMvc<SessionsViewMvc.Listener> {

    interface Listener : FinishSessionListener {
        fun onRecordNewSessionClicked()
        fun onExploreNewSessionsClicked()
        fun onSwipeToRefreshTriggered()
        fun onDisconnectSessionClicked(localSession: LocalSession)
        fun addNoteClicked(localSession: LocalSession)
        fun onReconnectSessionClicked(localSession: LocalSession)
        fun onEditSessionClicked(localSession: LocalSession)
        fun onShareSessionClicked(localSession: LocalSession)
        fun onDeleteSessionClicked(localSession: LocalSession)
        fun onFollowButtonClicked(localSession: LocalSession)
        fun onUnfollowButtonClicked(localSession: LocalSession)
        fun onMapButtonClicked(localSession: LocalSession, sensorName: String?)
        fun onGraphButtonClicked(localSession: LocalSession, sensorName: String?)
        fun onExpandSessionCard(localSession: LocalSession)
    }

    fun showSessionsView(localSessions: List<LocalSession>, sensorThresholds: HashMap<String, SensorThreshold>)
    fun showEmptyView()
    fun showLoaderFor(localSession: LocalSession)
    fun hideLoaderFor(localSession: LocalSession)
    fun hideLoaderFor(deviceId: String)
    fun reloadSession(localSession: LocalSession)
    fun showReconnectingLoaderFor(localSession: LocalSession)
    fun hideReconnectingLoaderFor(localSession: LocalSession)
    fun showLoader()
    fun hideLoader()
}
