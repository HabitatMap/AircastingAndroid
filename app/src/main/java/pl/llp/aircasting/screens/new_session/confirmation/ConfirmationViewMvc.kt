package pl.llp.aircasting.screens.new_session.confirmation

import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.common.ObservableViewMvc


interface ConfirmationViewMvc : ObservableViewMvc<ConfirmationViewMvc.Listener> {
    interface Listener {
        fun onStartRecordingClicked(session: Session)
    }

    fun updateLocation(latitude: Double?, longitude: Double?)
    fun onDestroy()
}
