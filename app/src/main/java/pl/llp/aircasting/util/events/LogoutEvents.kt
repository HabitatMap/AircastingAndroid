package pl.llp.aircasting.util.events

class LogoutEvent(
    private val isInProgress: Boolean = true,
    private val isAfterAccountDeletion: Boolean = false
) {
    val inProgress get() = isInProgress
    val afterAccountDeletion get() = isAfterAccountDeletion
}
