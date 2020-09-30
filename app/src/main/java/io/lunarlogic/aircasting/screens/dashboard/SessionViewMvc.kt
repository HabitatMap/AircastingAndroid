package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface SessionViewMvc<ListenerType>: ObservableViewMvc<ListenerType> {
    fun bindSession(sessionPresenter: SessionPresenter)
    fun showLoader()
    fun hideLoader()
}
