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

//    fun insertMeasurementsOnFollow(sessionId: Long, streamId: Long, measurements: List<Measurement>) {
//        measurements.forEach { measurement ->
//            insert(streamId, sessionId, measurement)
//        }
//    }

    fun deleteBySessionId(sessionId: Long?) {
        sessionId?.let {
            mDatabase.activeSessionsMeasurements().deleteActiveSessionMeasurementsBySession(sessionId)
        }
    }

    fun createOrReplaceMultipleRows(measurementStreamId: Long, sessionId: Long, measurements: List<Measurement>) { // todo: measurements is a list of few measurements often, the algorithm may not be proper
        val lastMeasurementsCount = mDatabase.activeSessionsMeasurements().countBySessionAndStream(sessionId, measurementStreamId)
        val measurementsToBeInserted = measurements.size // is this necesary? / proper- we always have 540 measurements for fixed/following sessions
        // maybe i should just search for oldest measurement index and replace measurement.size indexów od najstarszego measurementu?
        val measurementsToBeReplaced = ACTIVE_SESSIONS_MEASUREMENTS_MAX_NUMBER - lastMeasurementsCount - measurementsToBeInserted

        // TODO: what if we got 60 measurements to add and 500 already in the list? this case needs to be handled
        //
        // maybe i might add this '60 measurements' and delete a group of oldest measurements so its 540?
        if((lastMeasurementsCount + measurements.size) > ACTIVE_SESSIONS_MEASUREMENTS_MAX_NUMBER) {
            // todo: list of active measurement db objects
            // some measurements are about to be replaced, some inserted, so i have to provide 2 operations on 2 measurements' sublists
            // which one should be replaced, which inserted? (0,measurementsToBe...) or (measurementsToBe..., size-1)?? todo: i would say the line doesnt make a sense at all
//            insertAll(measurementStreamId, sessionId, measurements.subList(0, measurementsToBeInserted))
            val measurementsToBeReplaced = mutableListOf<ActiveSessionMeasurementDBObject>()
            measurements.forEach { measurement -> // case when "(measurementsToBeInserted + 1)" = 1 and "measurements.size-1" = 0 crashes
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
