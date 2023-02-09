package pl.llp.aircasting.data.local.repository

import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.entity.MeasurementDBObject
import pl.llp.aircasting.data.model.Measurement
import java.util.*

interface MeasurementsRepository {
    suspend fun insert(measurementStreamId: Long, sessionId: Long, measurement: Measurement): Long
}

class MeasurementsRepositoryImpl : MeasurementsRepository {
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

    override suspend fun insert(measurementStreamId: Long, sessionId: Long, measurement: Measurement): Long {
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

    fun getLastMeasurementsWithGivenAveragingFrequency(streamId: Long, limit: Int, averagingFrequency: Int): List<MeasurementDBObject?> {
        return mDatabase.measurements().getLastMeasurementsWithGivenAveragingFrequency(streamId, limit, averagingFrequency)
    }

    fun getBySessionIdAndStreamId(sessionId: Long, measurementStreamId: Long): List<MeasurementDBObject?> {
        return mDatabase.measurements().getBySessionIdAndStreamId(sessionId, measurementStreamId)
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
