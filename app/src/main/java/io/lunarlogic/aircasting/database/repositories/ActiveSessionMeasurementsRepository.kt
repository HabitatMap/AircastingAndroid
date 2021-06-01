package io.lunarlogic.aircasting.database.repositories

import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.ActiveSessionMeasurementDBObject
import io.lunarlogic.aircasting.models.Measurement
import java.util.*

class ActiveSessionMeasurementsRepository {
    private val ACTIVE_SESSIONS_MEASUREMENTS_MAX_NUMBER = 60 * 9 //we only need 9 mins of measurements. TODO: we should calculate that number based on time
    private val mDatabase = DatabaseProvider.get()

    fun insert(measurementStreamId: Long, sessionId: Long, measurement: Measurement): Long {
        val activeSessionMeasurementDBObject = ActiveSessionMeasurementDBObject(
            measurementStreamId,
            sessionId,
            measurement.value,
            measurement.time,
            measurement.latitude,
            measurement.longitude
        )

        return mDatabase.activeSessionsMeasurements().insert(activeSessionMeasurementDBObject)
    }

    private fun deleteAndInsert(measurement: ActiveSessionMeasurementDBObject) {
        mDatabase.activeSessionsMeasurements().deleteAndInsertInTransaction(measurement)
    }

    fun createOrReplace(sessionId: Long, streamId: Long, measurement: Measurement) {
        val lastMeasurementsCount = mDatabase.activeSessionsMeasurements().countBySessionAndStream(sessionId, streamId)

        if (lastMeasurementsCount > ACTIVE_SESSIONS_MEASUREMENTS_MAX_NUMBER ) {
            val activeSessionMeasurementDBObject = ActiveSessionMeasurementDBObject(
                streamId,
                sessionId,
                measurement.value,
                measurement.time,
                measurement.latitude,
                measurement.longitude
            )
            deleteAndInsert(activeSessionMeasurementDBObject)
        } else {
            insert(streamId, sessionId, measurement)
        }
    }

    fun deleteBySessionId(sessionId: Long?) {
        sessionId?.let {
            mDatabase.activeSessionsMeasurements().deleteActiveSessionMeasurementsBySession(sessionId)
        }
    }

}
