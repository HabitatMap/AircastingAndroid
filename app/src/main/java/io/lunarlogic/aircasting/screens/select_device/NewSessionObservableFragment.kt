package io.lunarlogic.aircasting.screens.select_device

interface NewSessionObservableFragment<ListenerType> {
    fun registerListener(listener: ListenerType)
    fun unregisterListener(listener: ListenerType)
}