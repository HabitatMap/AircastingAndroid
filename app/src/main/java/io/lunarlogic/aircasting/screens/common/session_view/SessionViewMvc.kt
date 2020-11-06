package io.lunarlogic.aircasting.screens.common.session_view

import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter

interface SessionViewMvc  {
    fun bindSession(sessionPresenter: SessionPresenter?)

    fun addMeasurement(measurement: Measurement)
}
