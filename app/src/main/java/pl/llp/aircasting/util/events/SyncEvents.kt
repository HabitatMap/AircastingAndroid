package pl.llp.aircasting.util.events

open class SessionsSyncEvent(private val isInProgress: Boolean = true) {
    val inProgress get() = isInProgress
    override fun toString(): String {
        return "SessionsSyncEvent(isInProgress=$isInProgress)"
    }
}
