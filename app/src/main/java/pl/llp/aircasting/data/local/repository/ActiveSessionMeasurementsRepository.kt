package pl.llp.aircasting.data.local.repository

import pl.llp.aircasting.data.api.util.Constants
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.entity.ActiveSessionMeasurementDBObject
import pl.llp.aircasting.data.local.entity.MeasurementDBObject
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import java.util.*

class ActiveSessionMeasurementsRepository {
    companion object {
        // We get 10 hours/minutes of Measurements for chart (we display 9 dots)
        // 10 hours are needed because we need to cut off last unfinished hour and
        // still get 540 measurements in total, so one hour acts as a buffer
        const val MAX_MEASUREMENTS_PER_STREAM_NUMBER = Constants.MEASUREMENTS_IN_HOUR * 10
    }

    private val mDatabase = DatabaseProvider.get()

    suspend fun insert(measurementStreamId: Long, sessionId: Long, measurement: Measurement): Long {
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

    fun insertAll(measurementStreamId: Long, sessionId: Long, measurements: List<Measurement>) {
        val measurementDBObjects = measurements.map { measurement ->
            ActiveSessionMeasurementDBObject(
                measurementStreamId,
                sessionId,
                measurement.value,
                measurement.time,
                measurement.latitude,
                measurement.longitude
            )
        }

        mDatabase.activeSessionsMeasurements().insertAll(measurementDBObjects)
    }

    private suspend fun deleteAndInsert(measurement: ActiveSessionMeasurementDBObject) {
        mDatabase.activeSessionsMeasurements().deleteAndInsertInTransaction(measurement)
    }

    suspend fun createOrReplace(sessionId: Long, streamId: Long, measurement: Measurement) {
        val lastMeasurementsCount =
            mDatabase.activeSessionsMeasurements().countBySessionAndStreamSuspend(sessionId, streamId)

        if (lastMeasurementsCount > MAX_MEASUREMENTS_PER_STREAM_NUMBER) {
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

    fun lastMeasurementTime(sessionId: Long, measurementStreamId: Long): Date? {
        return mDatabase.activeSessionsMeasurements()
            .getEndTimeOfMeasurementStream(sessionId, measurementStreamId)
    }

    fun deleteBySessionId(sessionId: Long?) {
        sessionId?.let {
            mDatabase.activeSessionsMeasurements()
                .deleteActiveSessionMeasurementsBySession(sessionId)
        }
    }

    fun createOrReplaceMultipleRows(
        measurementStreamId: Long,
        sessionId: Long,
        measurements: List<Measurement>
    ) {
        val numberOfMeasurementsAlreadyInTable = mDatabase.activeSessionsMeasurements()
            .countBySessionAndStream(sessionId, measurementStreamId)
        var measurementsToLoad = measurements

        if (measurements.size > MAX_MEASUREMENTS_PER_STREAM_NUMBER) {
            measurementsToLoad = measurements.takeLast(MAX_MEASUREMENTS_PER_STREAM_NUMBER)
        }

        val numberOfMeasurementsToBePresentInTable = numberOfMeasurementsAlreadyInTable + measurementsToLoad.size

        if(numberOfMeasurementsToBePresentInTable > MAX_MEASUREMENTS_PER_STREAM_NUMBER) {
            deleteAndInsert(measurementStreamId, sessionId, measurementsToLoad)
        } else insertAll(measurementStreamId, sessionId, measurementsToLoad)
    }

    private fun deleteAndInsert(
        measurementStreamId: Long,
        sessionId: Long,
        measurements: List<Measurement>
    ) {
        val measurementDBObjects = measurements.map { measurement ->
            ActiveSessionMeasurementDBObject(
                measurementStreamId,
                sessionId,
                measurement.value,
                measurement.time,
                measurement.latitude,
                measurement.longitude
            )
        }

        mDatabase.activeSessionsMeasurements()
            .deleteAndInsertMultipleMeasurementsInTransaction(measurementDBObjects)
    }

    fun loadMeasurementsForStreams(
        sessionId: Long,
        measurementStreams: List<MeasurementStream>?,
        limit: Int
    ) {
        var measurements: List<Measurement> = mutableListOf()

        measurementStreams?.forEach { measurementStream ->
            val streamId =
                MeasurementStreamsRepository().getId(sessionId, measurementStream)

            streamId?.let { streamId ->
                measurements =
                    measurementsList(
                        MeasurementsRepositoryImpl().getLastMeasurementsForStream(
                            streamId,
                            limit
                        )
                    )
                insertAll(streamId, sessionId, measurements)
            }
        }
    }

    private fun measurementsList(measurements: List<MeasurementDBObject?>): List<Measurement> {
        return measurements.mapNotNull { measurementDBObject ->
            measurementDBObject?.let { measurement ->
                Measurement(measurement)
            }
        }
    }
}
