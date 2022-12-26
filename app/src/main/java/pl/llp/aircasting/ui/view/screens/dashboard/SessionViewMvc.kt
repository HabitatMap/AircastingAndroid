package pl.llp.aircasting.ui.view.screens.dashboard

import pl.llp.aircasting.ui.view.common.ObservableViewMvc

interface SessionViewMvc<ListenerType> : ObservableViewMvc<ListenerType> {
    fun bindSession(sessionPresenter: SessionPresenter)
    fun showLoader()
    fun hideLoader()
}