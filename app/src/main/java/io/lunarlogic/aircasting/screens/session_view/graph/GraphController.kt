package io.lunarlogic.aircasting.screens.session_view.graph

import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.models.*
import io.lunarlogic.aircasting.screens.session_view.SessionViewController
import io.lunarlogic.aircasting.screens.session_view.SessionViewMvc


class GraphController(
    rootActivity: AppCompatActivity,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionViewMvc,
    sessionUUID: String,
    sensorName: String?
): SessionViewController(rootActivity, mSessionsViewModel, mViewMvc, sessionUUID, sensorName) {
    override fun locateRequested() {}
}
