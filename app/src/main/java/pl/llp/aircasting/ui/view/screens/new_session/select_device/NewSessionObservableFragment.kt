package pl.llp.aircasting.ui.view.screens.new_session.select_device

interface NewSessionObservableFragment<ListenerType> {
    fun registerListener(listener: ListenerType)
    fun unregisterListener(listener: ListenerType)
}
