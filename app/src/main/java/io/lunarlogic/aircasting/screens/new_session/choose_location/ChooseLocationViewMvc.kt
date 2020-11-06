package io.lunarlogic.aircasting.screens.new_session.choose_location

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.models.Session


interface ChooseLocationViewMvc : ObservableViewMvc<ChooseLocationViewMvc.Listener> {

    interface Listener {
        fun onContinueClicked(session: Session)
    }
}
