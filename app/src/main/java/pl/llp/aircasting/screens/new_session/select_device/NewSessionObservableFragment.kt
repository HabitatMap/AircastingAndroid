package pl.llp.aircasting.screens.new_session.select_device

interface NewSessionObservableFragment<ListenerType> {
    fun registerListener(listener: ListenerType)
    fun unregisterListener(listener: ListenerType)
}
