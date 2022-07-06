package pl.llp.aircasting.ui.view.screens.dashboard.active

import pl.llp.aircasting.data.model.Session

interface FinishSessionListener {
    fun onFinishSessionConfirmed(session: Session) { /* Do nothing */ }
    fun onFinishAndSyncSessionConfirmed(session: Session) { /* Do nothing */ }
}
