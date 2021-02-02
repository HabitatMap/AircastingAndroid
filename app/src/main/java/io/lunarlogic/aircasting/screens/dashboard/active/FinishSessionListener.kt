package io.lunarlogic.aircasting.screens.dashboard.active

import io.lunarlogic.aircasting.models.Session

interface FinishSessionListener {
    fun onFinishSessionConfirmed(session: Session)
    fun onFinishAndSyncSessionConfirmed(session: Session)
}
