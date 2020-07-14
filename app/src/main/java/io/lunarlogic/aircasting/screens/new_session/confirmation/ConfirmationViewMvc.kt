package io.lunarlogic.aircasting.screens.new_session.confirmation

import io.lunarlogic.aircasting.sensor.Session
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc


interface ConfirmationViewMvc : ObservableViewMvc<ConfirmationViewMvc.Listener> {
    interface Listener {
        fun onStartRecordingClicked(session: Session)
    }
}