package io.lunarlogic.aircasting.screens.session_view.graph

import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.models.*
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewController
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvc


class GraphController(
    rootActivity: AppCompatActivity,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionDetailsViewMvc,
    sessionUUID: String,
    sensorName: String?
): SessionDetailsViewController(rootActivity, mSessionsViewModel, mViewMvc, sessionUUID, sensorName) { //todo: add SessionsViewMvc.Listener here to get addNoteClicked and onFinishSessionConfirmed <?>
    override fun locateRequested() {}
}
