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

    fun getNonAveragedPreviousMeasurementsCount(sessionId: Long, crossingThresholdTime: Date, newAveragingFrequency: Int): Int {
        return mDatabase.measurements().getNonAveragedPreviousMeasurementsCount(sessionId, crossingThresholdTime, newAveragingFrequency )
    }

    fun getNonAveragedCurrentMeasurementsCount(sessionId: Long, crossingThresholdTime: Date): Int {
        return mDatabase.measurements().getNonAveragedCurrentMeasurementsCount(sessionId, crossingThresholdTime )
    }

    fun getNonAveragedPreviousMeasurements(streamId: Long, averagingFrequency: Int, thresholdCrossingTime: Date): List<MeasurementDBObject> {
        return mDatabase.measurements().getNonAveragedPreviousMeasurements(streamId, averagingFrequency, thresholdCrossingTime)
    }

    fun getNonAveragedCurrentMeasurements(streamId: Long, averagingFrequency: Int, thresholdCrossingTime: Date): List<MeasurementDBObject> {
        return mDatabase.measurements().getNonAveragedCurrentMeasurements(streamId, averagingFrequency, thresholdCrossingTime)
    }

    fun deleteMeasurements(streamId: Long, measurementsIds: List<Long>) {
        mDatabase.measurements().deleteInTransaction(streamId, measurementsIds)
    }

    fun averageMeasurement(measurementId: Long, value: Double, averagingFrequency: Int) {
        mDatabase.measurements().averageMeasurement(measurementId, value, averagingFrequency)
    }

    fun getAllByStreamId(streamId: Long): List<MeasurementDBObject> {
        return mDatabase.measurements().getByStreamId(streamId)
    }

}
