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

    fun createOrReplaceMultipleRows(measurementStreamId: Long, sessionId: Long, measurements: List<Measurement>) { // todo: measurements is a list of few measurements often, the algorithm may not be proper
        val lastMeasurementsCount = mDatabase.activeSessionsMeasurements().countBySessionAndStream(sessionId, measurementStreamId)
        // todo: maybe i should just search for oldest measurement index and replace measurement.size indexów od najstarszego measurementu?
        // TODO: what if we got 60 measurements to add and 500 already in the list? this case needs to be handle
        // maybe i might add this '60 measurements' and delete a group of oldest measurements so its 540?

        if((lastMeasurementsCount + measurements.size) > ACTIVE_SESSIONS_MEASUREMENTS_MAX_NUMBER) {
            // todo: list of active measurement db objects
//            insertAll(measurementStreamId, sessionId, measurements.subList(0, measurementsToBeInserted))
            val measurementsToBeReplaced = mutableListOf<ActiveSessionMeasurementDBObject>()
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
