package pl.llp.aircasting.database.repositories

import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.data_classes.ActiveSessionMeasurementDBObject
import pl.llp.aircasting.database.data_classes.MeasurementDBObject
import pl.llp.aircasting.models.Measurement
import pl.llp.aircasting.models.MeasurementStream

class ActiveSessionMeasurementsRepository {
    companion object {
        // We get 10 hours/minutes of Measurements for chart, to calculate first hour as full
        const val MAX_MEASUREMENTS_NUMBER = 60 * 10
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

    private fun deleteAndInsertMultipleRows(measurements: List<ActiveSessionMeasurementDBObject>) {
        mDatabase.activeSessionsMeasurements().deleteAndInsertMultipleMeasurementsInTransaction(measurements)
    }

    fun createOrReplace(sessionId: Long, streamId: Long, measurement: Measurement) {
        val lastMeasurementsCount = mDatabase.activeSessionsMeasurements().countBySessionAndStream(sessionId, streamId)

        if (lastMeasurementsCount > MAX_MEASUREMENTS_NUMBER ) {
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

    fun createOrReplaceMultipleRows(measurementStreamId: Long, sessionId: Long, measurements: List<Measurement>) {
        var measurementsToBeReplaced = measurements

        if(measurements.size > MAX_MEASUREMENTS_NUMBER) {
             measurementsToBeReplaced = measurements.takeLast(MAX_MEASUREMENTS_NUMBER)
        }

        val measurementsDbObjectsToBeReplaced = measurementsToBeReplaced.map { measurement ->
            ActiveSessionMeasurementDBObject(
                measurementStreamId,
                sessionId,
                measurement.value,
                measurement.time,
                measurement.longitude,
                measurement.latitude
            )
        }
        deleteAndInsertMultipleRows(measurementsDbObjectsToBeReplaced)
    }

    fun loadMeasurementsForStreams(
        sessionId: Long,
        measurementStreams: List<MeasurementStream>?,
        limit: Int
    ) {
        var measurements:  List<Measurement> = mutableListOf()

        measurementStreams?.forEach { measurementStream ->
            val streamId =
                MeasurementStreamsRepository().getId(sessionId, measurementStream)

            streamId?.let { streamId ->
                measurements =
                   measurementsList(
                        MeasurementsRepository().getLastMeasurementsForStream(
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
