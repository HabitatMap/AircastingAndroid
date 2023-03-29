package pl.llp.aircasting.data.local.repository

import android.util.Log
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.entity.MeasurementDBObject
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.helpers.services.AveragingWindow
import java.util.*
import javax.inject.Inject

interface MeasurementsRepository {
    suspend fun insert(measurementStreamId: Long, sessionId: Long, measurement: Measurement): Long
}

@UserSessionScope
class MeasurementsRepositoryImpl @Inject constructor() : MeasurementsRepository {
    private val mDatabase = DatabaseProvider.get()

    suspend fun insertAll(measurementStreamId: Long, sessionId: Long, measurements: List<Measurement>) {
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

    suspend fun lastMeasurementTime(sessionId: Long?): Date? {
        sessionId ?: return null

        val measurement = mDatabase.measurements().lastForSession(sessionId)
        return measurement?.time
    }

    suspend fun lastMeasurementTime(sessionId: Long, measurementStreamId: Long): Date? {
        val measurement = mDatabase.measurements().lastForStream(sessionId, measurementStreamId)
        return measurement?.time
    }

    suspend fun getLastMeasurementsForStream(streamId: Long, limit: Int): List<MeasurementDBObject?> {
        return mDatabase.measurements().getLastMeasurements(streamId, limit)
    }

    suspend fun getLastMeasurementsWithGivenAveragingFrequency(streamId: Long, limit: Int, averagingFrequency: Int): List<MeasurementDBObject?> {
        return mDatabase.measurements().getLastMeasurementsWithGivenAveragingFrequency(streamId, limit, averagingFrequency)
    }

    suspend fun getBySessionIdAndStreamId(sessionId: Long, measurementStreamId: Long): List<MeasurementDBObject?> {
        return mDatabase.measurements().getBySessionIdAndStreamId(sessionId, measurementStreamId)
    }

    suspend fun deleteMeasurementsOlderThan(
        streamId: Long,
        lastExpectedMeasurementTime: Date
    ) {
        mDatabase.measurements().deleteInTransaction(streamId, lastExpectedMeasurementTime)
    }

    suspend fun getNonAveragedPreviousMeasurementsCount(sessionId: Long, crossingThresholdTime: Date, newAveragingFrequency: Int): Int {
        return mDatabase.measurements().getNonAveragedPreviousMeasurementsCount(sessionId, crossingThresholdTime, newAveragingFrequency )
    }

    suspend fun getNonAveragedPreviousMeasurements(streamId: Long, averagingFrequency: Int, thresholdCrossingTime: Date): List<MeasurementDBObject> {
        return mDatabase.measurements().getNonAveragedPreviousMeasurements(streamId, averagingFrequency, thresholdCrossingTime)
    }

    suspend fun getNonAveragedCurrentMeasurements(streamId: Long, averagingFrequency: Int, thresholdCrossingTime: Date): List<MeasurementDBObject> {
        return mDatabase.measurements().getNonAveragedCurrentMeasurements(streamId, averagingFrequency, thresholdCrossingTime)
    }

    suspend fun getMeasurementsToAverage(streamId: Long, averagingWindow: AveragingWindow): List<MeasurementDBObject> {
        return mDatabase.measurements().getMeasurementsToAverage(streamId, averagingWindow.value)
    }

    suspend fun deleteMeasurements(streamId: Long, measurementsIds: List<Long>) {
        mDatabase.measurements().deleteInTransaction(streamId, measurementsIds)
    }

    suspend fun deleteMeasurementsSuspend(streamId: Long, measurementsIds: List<Long>) {
        mDatabase.measurements().deleteMeasurementsSuspend(streamId, measurementsIds)
    }

    suspend fun averageMeasurement(measurementId: Long, value: Double, averagingFrequency: Int, time: Date?) {
        if (time == null) {
            Log.e(TAG, "time was null")
            return
        }
        mDatabase.measurements().averageMeasurement(measurementId, value, averagingFrequency, time)
    }

    suspend fun getAllByStreamId(streamId: Long): List<MeasurementDBObject> {
        return mDatabase.measurements().getByStreamId(streamId)
    }
}
