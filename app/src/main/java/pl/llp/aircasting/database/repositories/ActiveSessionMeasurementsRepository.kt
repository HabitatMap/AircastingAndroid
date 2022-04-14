package pl.llp.aircasting.database.repositories

import org.apache.commons.lang3.time.DateUtils
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.data_classes.ActiveSessionMeasurementDBObject
import pl.llp.aircasting.database.data_classes.MeasurementDBObject
import pl.llp.aircasting.models.Measurement
import pl.llp.aircasting.models.MeasurementStream
import java.util.*

class ActiveSessionMeasurementsRepository {
    companion object {
        // We get 9 hours/minutes of Measurements for chart (we display 9 dots)
        const val MAX_MEASUREMENTS_PER_STREAM_NUMBER = 60 * 9
    }

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

    private fun deleteAndInsert(measurement: ActiveSessionMeasurementDBObject) {
        mDatabase.activeSessionsMeasurements().deleteAndInsertInTransaction(measurement)
    }

    fun createOrReplace(sessionId: Long, streamId: Long, measurement: Measurement) {
        val lastMeasurementsCount =
            mDatabase.activeSessionsMeasurements().countBySessionAndStream(sessionId, streamId)

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
                val lastMeasurementTime = MeasurementsRepository().lastMeasurementTime(sessionId, streamId)
                val lastMeasurementHour = DateUtils.truncate(lastMeasurementTime, Calendar.HOUR_OF_DAY)

                measurements =
                    measurementsList(
                        MeasurementsRepository().getLastMeasurementsForStreamStartingFromHour(
                            streamId,
                            limit,
                            lastMeasurementHour
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
