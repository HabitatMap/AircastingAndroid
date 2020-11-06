package io.lunarlogic.aircasting.screens.new_session.confirmation

import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc


interface ConfirmationViewMvc : ObservableViewMvc<ConfirmationViewMvc.Listener> {
    interface Listener {
        fun onStartRecordingClicked(session: Session)
    }

    fun updateLocation(latitude: Double?, longitude: Double?)
}
