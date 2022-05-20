package pl.llp.aircasting.ui.view.common

interface ObservableViewMvc<ListenerType> : ViewMvc {

    fun registerListener(listener: ListenerType)

    fun unregisterListener(listener: ListenerType)
}
