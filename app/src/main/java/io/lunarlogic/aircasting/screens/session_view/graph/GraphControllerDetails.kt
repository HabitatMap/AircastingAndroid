package io.lunarlogic.aircasting.screens.session_view.graph

import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.models.*
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewController
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvc


class GraphControllerDetails(
    rootActivity: AppCompatActivity,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionDetailsViewMvc,
    sessionUUID: String,
    sensorName: String?
): SessionDetailsViewController(rootActivity, mSessionsViewModel, mViewMvc, sessionUUID, sensorName) {
    override fun locateRequested() {}
}
