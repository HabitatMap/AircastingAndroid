package pl.llp.aircasting.database.repositories

import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.data_classes.ActiveSessionMeasurementDBObject
import pl.llp.aircasting.database.data_classes.MeasurementDBObject
import pl.llp.aircasting.models.Measurement

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

    fun createOrReplaceMultipleRows(measurementStreamId: Long, sessionId: Long, measurements: List<Measurement>) {
        val lastMeasurementsCount = mDatabase.activeSessionsMeasurements().countBySessionAndStream(sessionId, measurementStreamId)
        val measurementsToBeReplaced = mutableListOf<ActiveSessionMeasurementDBObject>()

        if(measurements.size > ACTIVE_SESSIONS_MEASUREMENTS_MAX_NUMBER) {
            measurements.takeLast(ACTIVE_SESSIONS_MEASUREMENTS_MAX_NUMBER).forEach { measurement ->
                measurementsToBeReplaced.add(
                    ActiveSessionMeasurementDBObject(
                        measurementStreamId,
                        sessionId,
                        measurement.value,
                        measurement.time,
                        measurement.longitude,
                        measurement.latitude
                    )
                )
            }
            deleteAndInsertMultipleRows(measurementsToBeReplaced)
            return
        }

        if((lastMeasurementsCount + measurements.size) > ACTIVE_SESSIONS_MEASUREMENTS_MAX_NUMBER) {
            measurements.forEach { measurement ->
                measurementsToBeReplaced.add(
                    ActiveSessionMeasurementDBObject(
                        measurementStreamId,
                        sessionId,
                        measurement.value,
                        measurement.time,
                        measurement.latitude,
                        measurement.longitude
                    )
                )
            }
            deleteAndInsertMultipleRows(measurementsToBeReplaced)
        } else {
            insertAll(measurementStreamId, sessionId, measurements)
        }
    }

}
