package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.screens.common.ViewMvc
import io.lunarlogic.aircasting.sensor.Session

interface DashboardViewMvc : ViewMvc {
    fun goToTab(sessionType: Session.Type, sessionStatus: Session.Status = Session.Status.RECORDING)
}