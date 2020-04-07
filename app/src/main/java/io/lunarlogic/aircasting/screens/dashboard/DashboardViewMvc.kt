package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc


interface DashboardViewMvc : ObservableViewMvc<DashboardViewMvc.Listener> {

    interface Listener {
        fun onRecordNewSessionClicked()
    }
}