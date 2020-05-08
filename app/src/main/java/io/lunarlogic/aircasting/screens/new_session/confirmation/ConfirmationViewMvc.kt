package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.sensor.Session
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc


interface ConfirmationViewMvc : ObservableViewMvc<ConfirmationViewMvc.Listener> {
    interface Listener {
        fun onStartRecordingClicked(session: Session)
    }
}