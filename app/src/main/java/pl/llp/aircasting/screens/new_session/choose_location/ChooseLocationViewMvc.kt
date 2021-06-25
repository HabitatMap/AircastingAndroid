package pl.llp.aircasting.screens.new_session.choose_location

import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.common.ObservableViewMvc


interface ChooseLocationViewMvc : ObservableViewMvc<ChooseLocationViewMvc.Listener> {

    interface Listener {
        fun onContinueClicked(session: Session)
    }

    fun onDestroy()
}
