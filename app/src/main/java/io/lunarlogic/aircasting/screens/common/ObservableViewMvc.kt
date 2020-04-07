package io.lunarlogic.aircasting.screens.common

interface ObservableViewMvc<ListenerType> : ViewMvc {

    fun registerListener(listener: ListenerType)

    fun unregisterListener(listener: ListenerType)
}
