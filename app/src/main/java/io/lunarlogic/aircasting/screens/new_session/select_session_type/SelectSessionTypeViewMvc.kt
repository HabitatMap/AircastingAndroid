package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc


interface SelectSessionTypeViewMvc : ObservableViewMvc<SelectSessionTypeViewMvc.Listener> {

    interface Listener {
        fun onFixedSessionSelected()
        fun onMobileSessionSelected()
    }
}