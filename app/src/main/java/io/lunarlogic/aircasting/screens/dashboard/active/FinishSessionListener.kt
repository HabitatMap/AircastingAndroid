package io.lunarlogic.aircasting.screens.dashboard.active

import io.lunarlogic.aircasting.models.Session

interface FinishSessionListener {
    fun onStopSessionClicked(session: Session)
}
