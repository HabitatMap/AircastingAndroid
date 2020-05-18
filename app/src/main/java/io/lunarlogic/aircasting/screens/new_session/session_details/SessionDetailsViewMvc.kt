package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc


interface SessionDetailsViewMvc : ObservableViewMvc<SessionDetailsViewMvc.Listener> {

    interface Listener {
        fun onSessionDetailsContinueClicked(sessionName: String, sessionTags: ArrayList<String>)
    }
}