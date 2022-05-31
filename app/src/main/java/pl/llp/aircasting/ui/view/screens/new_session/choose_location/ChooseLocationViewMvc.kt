package pl.llp.aircasting.ui.view.screens.new_session.choose_location

import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.ObservableViewMvc


interface ChooseLocationViewMvc : ObservableViewMvc<ChooseLocationViewMvc.Listener> {

    interface Listener {
        fun onContinueClicked(session: Session)
    }

    fun onDestroy()
}
