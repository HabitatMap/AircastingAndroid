package pl.llp.aircasting.util.events

class SessionsSyncEvent(private val isInProgress: Boolean = true) {
    val inProgress get() = isInProgress
}
class SessionsSyncErrorEvent
class SessionsSyncSuccessEvent
