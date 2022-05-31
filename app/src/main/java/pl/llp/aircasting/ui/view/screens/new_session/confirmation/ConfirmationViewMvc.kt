package pl.llp.aircasting.ui.view.screens.new_session.confirmation

import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.common.ObservableViewMvc


interface ConfirmationViewMvc : ObservableViewMvc<ConfirmationViewMvc.Listener> {
    interface Listener {
        fun onStartRecordingClicked(localSession: LocalSession)
    }

    fun updateLocation(latitude: Double?, longitude: Double?)
    fun onDestroy()
}
