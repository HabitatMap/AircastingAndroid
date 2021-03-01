package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.database.data_classes.MeasurementDBObject
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository

class RemoveOldMeasurementsService() {
    private val TWENTY_FOUR_HOURS_IN_MILLISECONDS = 24  *  60  *  60  *  1000
    private val measurementRepository = MeasurementsRepository()

    fun removeMeasurementsFromSessions(fixedSessionsIds: List<Long>) {
        val fixedSessionsMeasurements = getFixedSessionsMeasurements(fixedSessionsIds)
        val measurementsForDeletionIds = findMeasurementsForDeletion(fixedSessionsMeasurements)
        if (measurementsForDeletionIds.isNotEmpty()) {
            measurementRepository.deleteMeasurements(measurementsForDeletionIds)
        }
    }

    private fun getFixedSessionsMeasurements(fixedSessionsIds: List<Long>): List<MeasurementDBObject> {
        return measurementRepository.getMeasurementsBySessionsIds(fixedSessionsIds)
    }

    private fun findMeasurementsForDeletion(fixedSessionsMeasurements: List<MeasurementDBObject>): List<Long> {
        val measurementsIdsForDeletion: MutableList<Long> = mutableListOf()
        fixedSessionsMeasurements.forEach { measurement ->
            // `measurement.time.time` looks weird, but it's just getTime() current syntax
            // it returns the number of milliseconds since January 1, 1970, 00:00:00 GMT represented by this Date object.
            // Source: https://developer.android.com/reference/kotlin/java/util/Date
            val measurementTimeInMilliseconds = measurement.time.time
            if (measurementOlderThanTwentyFourHours(measurementTimeInMilliseconds)) {
                measurementsIdsForDeletion.add(measurement.id)
            }
        }
        return measurementsIdsForDeletion
    }

    private fun measurementOlderThanTwentyFourHours(measurementTimeInMilliseconds: Long): Boolean {
        val currentTimeInMilliseconds = System.currentTimeMillis()
        val timeDifference = currentTimeInMilliseconds.minus(measurementTimeInMilliseconds)
        return timeDifference >= TWENTY_FOUR_HOURS_IN_MILLISECONDS
    }
}
