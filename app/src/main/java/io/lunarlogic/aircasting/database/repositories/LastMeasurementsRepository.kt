package io.lunarlogic.aircasting.database.repositories

import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.ActiveSessionMeasurementDBObject
import io.lunarlogic.aircasting.models.Measurement
import java.util.*

class LastMeasurementsRepository {
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

    private fun getOldestMeasurementID(sessionId: Long, streamId: Long): Int {
        return mDatabase.activeSessionsMeasurements().getOldestMeasurementId(sessionId, streamId)
    }

    fun deleteAndInsert(measurement: ActiveSessionMeasurementDBObject) {
        mDatabase.activeSessionsMeasurements().deleteAndInsertInTransaction(measurement)
    }

    fun createOrReplace(sessionId: Long, streamId: Long, measurement: Measurement) {
        val lastMeasurementsCount = mDatabase.activeSessionsMeasurements().countBySessionAndStream(sessionId, streamId)

        if (lastMeasurementsCount > 15 ) {
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

}
