package io.lunarlogic.aircasting.database.repositories

import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.MeasurementDBObject
import io.lunarlogic.aircasting.models.Measurement
import java.util.*

class MeasurementsRepository {
    private val mDatabase = DatabaseProvider.get()

    fun insertAll(measurementStreamId: Long, sessionId: Long, measurements: List<Measurement>) {
        val measurementDBObjects = measurements.map { measurement ->
            MeasurementDBObject(
                measurementStreamId,
                sessionId,
                measurement.value,
                measurement.time,
                measurement.latitude,
                measurement.longitude,
                measurement.averagingFrequency
            )
        }

        mDatabase.measurements().insertAll(measurementDBObjects)
    }

    fun insert(measurementStreamId: Long, sessionId: Long, measurement: Measurement): Long {
        val measurementDBObject = MeasurementDBObject(
            measurementStreamId,
            sessionId,
            measurement.value,
            measurement.time,
            measurement.latitude,
            measurement.longitude,
            measurement.averagingFrequency
        )

        return mDatabase.measurements().insert(measurementDBObject)
    }

    fun lastMeasurementTime(sessionId: Long): Date? {
        val measurement = mDatabase.measurements().lastForSession(sessionId)
        return measurement?.time
    }

    fun lastMeasurementTime(sessionId: Long, measurementStreamId: Long): Date? {
        val measurement = mDatabase.measurements().lastForStream(sessionId, measurementStreamId)
        return measurement?.time
    }

    fun getLastMeasurementsForStream(streamId: Long, limit: Int): List<MeasurementDBObject?> {
        return mDatabase.measurements().getLastMeasurements(streamId, limit)
    }

    fun deleteMeasurementsOlderThan(
        streamId: Long,
        lastExpectedMeasurementTime: Date
    ) {
        mDatabase.measurements().deleteInTransaction(streamId, lastExpectedMeasurementTime)
    }

    fun getNonAveragedMeasurementsCount(sessionId: Long, averagingFrequency: Int): Int {
        return mDatabase.measurements().getNonAveragedMeasurementsCount(sessionId, averagingFrequency )
    }

    fun getNonAveragedMeasurements(streamId: Long, averagingFrequency: Int): List<MeasurementDBObject> {
        return mDatabase.measurements().getNonAveragedMeasurements(streamId, averagingFrequency)
    }

    fun deleteMeasurements(streamId: Long, measurementsIds: List<Long>) {
        mDatabase.measurements().deleteInTransaction(streamId, measurementsIds)
    }

    fun averageMeasurement(measurementId: Long, value: Double, averagingFrequency: Int) {
        mDatabase.measurements().averageMeasurement(measurementId, value, averagingFrequency)
    }

}
