package pl.llp.aircasting.ui.view.screens.dashboard

import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.observers.SessionsObserver
import pl.llp.aircasting.ui.view.common.ObservableViewMvc
import pl.llp.aircasting.ui.view.screens.dashboard.active.FinishSessionListener

interface SessionsViewMvc : ObservableViewMvc<SessionsViewMvc.Listener> {

    interface Listener : FinishSessionListener {
        fun onRecordNewSessionClicked()
        fun onExploreNewSessionsClicked()
        fun onDisconnectSessionClicked(session: Session)
        fun addNoteClicked(session: Session)
        fun onReconnectSessionClicked(session: Session)
        fun onFollowButtonClicked(session: Session)
        fun onUnfollowButtonClicked(session: Session)
        fun onMapButtonClicked(session: Session, sensorName: String?)
        fun onGraphButtonClicked(session: Session, sensorName: String?)
        fun onExpandSessionCard(session: Session)
        fun onCollapseSessionCard(session: Session) { /* Do nothing */
        }
    }

    fun showSessionsView(
        modifiedSessions: Map<SessionsObserver.ModificationType, List<Session>>,
        sensorThresholds: Map<String, SensorThreshold>
    )

    fun showEmptyView()
    fun showLoaderFor(session: Session)
    fun hideLoaderFor(session: Session)
    fun hideLoaderFor(deviceId: String)
    fun reloadSession(session: Session)
    fun showReconnectingLoaderFor(session: Session)
    fun hideReconnectingLoaderFor(session: Session)
}
