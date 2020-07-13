package io.lunarlogic.aircasting.screens.new_session.select_session_type

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc


interface SelectSessionTypeViewMvc : ObservableViewMvc<SelectSessionTypeViewMvc.Listener> {

    interface Listener {
        fun onFixedSessionSelected()
        fun onMobileSessionSelected()
    }
}