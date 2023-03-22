package pl.llp.aircasting.util.events

open class SessionsSyncEvent(private val isInProgress: Boolean = true) {
    val inProgress get() = isInProgress
}
class SessionsSyncErrorEvent(val error: Throwable?): SessionsSyncEvent(false)
class SessionsSyncSuccessEvent: SessionsSyncEvent(false)
