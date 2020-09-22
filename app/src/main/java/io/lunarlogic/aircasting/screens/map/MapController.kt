package io.lunarlogic.aircasting.screens.map

import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.location.LocationHelper

class MapController(
    private val rootActivity: AppCompatActivity,
    private val mViewMvc: MapViewMvc,
    private val sessionUUID: String,
    private val sensorName: String
) {
    private val mSessionsRepository = SessionsRepository()

    fun onCreate() {
        DatabaseProvider.runQuery {
            val session = mSessionsRepository.loadSessionAndMeasurementsByUUID(sessionUUID)
            if (session != null) {
                mViewMvc.bindSession(session)

                val measurementStream = session.streams.firstOrNull { it.sensorName == sensorName }
                mViewMvc.bindMeasurementStream(measurementStream)
            }
        }
    }

    fun onDestroy() {}
}
