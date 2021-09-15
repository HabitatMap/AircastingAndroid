package pl.llp.aircasting.database.repositories

import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.data_classes.ActiveSessionMeasurementDBObject
import pl.llp.aircasting.database.data_classes.MeasurementDBObject
import pl.llp.aircasting.models.Measurement
import java.util.*

class ActiveSessionMeasurementsRepository {
    private val ACTIVE_SESSIONS_MEASUREMENTS_MAX_NUMBER = 60 * 9 //we only need 9 mins of measurements. TODO: we should calculate that number based on time
    private val mDatabase = DatabaseProvider.get()

    fun insertAll(measurementStreamId: Long, sessionId: Long, measurements: List<Measurement?>) {
        val activeMeasurementDbObjects = measurements.map { measurement ->
            ActiveSessionMeasurementDBObject(
                measurementStreamId,
                sessionId,
                measurement!!.value,
                measurement.time,
                measurement.latitude,
                measurement.longitude,
                measurement.averagingFrequency
            )
        }

        mDatabase.activeSessionsMeasurements().insertAll(activeMeasurementDbObjects)
    }

    fun getStreamId(sessionId: Long?, time: Date?, value: Double?): Long {
        return mDatabase.activeSessionsMeasurements().getStreamId(sessionId, value, time)
    }

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
    fun getNonAveragedCurrentMeasurements(streamId: Long, averagingFrequency: Int, thresholdCrossingTime: Date): List<ActiveSessionMeasurementDBObject> {
        return mDatabase.activeSessionsMeasurements().getNonAveragedCurrentMeasurements(streamId, averagingFrequency, thresholdCrossingTime)
    }

    fun deleteMeasurements(streamId: Long, measurementsIds: List<Long>) {
         mDatabase.activeSessionsMeasurements().deleteInTransaction(streamId, measurementsIds)
    }

}
