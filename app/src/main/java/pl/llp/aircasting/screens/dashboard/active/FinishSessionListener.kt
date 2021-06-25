package pl.llp.aircasting.screens.dashboard.active

import pl.llp.aircasting.models.Session

interface FinishSessionListener {
    fun onFinishSessionConfirmed(session: Session)
    fun onFinishAndSyncSessionConfirmed(session: Session)
}
