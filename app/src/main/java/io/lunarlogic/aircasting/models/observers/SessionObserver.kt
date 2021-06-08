package io.lunarlogic.aircasting.models.observers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import kotlinx.coroutines.CoroutineScope

class SessionObserver(
    private val mLifecycleOwner: LifecycleOwner,
    private val mSessionsViewModel: SessionsViewModel,
    private val mSessionPresenter: SessionPresenter,
    private val onSessionChangedCallback: (coroutineScope: CoroutineScope) -> Unit
) {
    fun observe() {
        val sessionUUID = mSessionPresenter.sessionUUID
        sessionUUID ?: return
        var session: Session

        mSessionsViewModel.loadSessionWithNotesAndStreamsByUUID(sessionUUID).observe(mLifecycleOwner, Observer { sessionDBObject ->
            sessionDBObject?.let {
                session = Session(sessionDBObject)
                if (session.hasChangedFrom(mSessionPresenter.session)) {
                    onSessionChanged(session)
                }
            }
        })
    }

    private fun onSessionChanged(session: Session) {
        mSessionPresenter.session = session

        DatabaseProvider.runQuery { coroutineScope ->
            var selectedSensorName = mSessionPresenter.initialSensorName
            if (mSessionPresenter.selectedStream != null) {
                selectedSensorName = mSessionPresenter.selectedStream!!.sensorName
            }

            val measurementStream =
                session.streams.firstOrNull { it.sensorName == selectedSensorName }
            mSessionPresenter.selectedStream = measurementStream

            val sensorThresholds = mSessionsViewModel.findOrCreateSensorThresholds(session)
            mSessionPresenter.setSensorThresholds(sensorThresholds)

            onSessionChangedCallback.invoke(coroutineScope)
        }
    }
}
