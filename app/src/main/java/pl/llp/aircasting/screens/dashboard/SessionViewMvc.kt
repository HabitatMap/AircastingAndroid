package pl.llp.aircasting.screens.dashboard

import pl.llp.aircasting.screens.common.ObservableViewMvc

interface SessionViewMvc<ListenerType>: ObservableViewMvc<ListenerType> {
    fun bindSession(sessionPresenter: SessionPresenter)
    fun showLoader()
    fun hideLoader()
}
