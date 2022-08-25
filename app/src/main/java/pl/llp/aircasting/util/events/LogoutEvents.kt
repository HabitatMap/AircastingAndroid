package pl.llp.aircasting.util.events

class LogoutEvent(private val isInProgress: Boolean = true) {
    val inProgress get() = isInProgress
}
