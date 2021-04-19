package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.models.Session
import java.util.*

class RemoveOldMeasurementsService() {
    private val measurementRepository = MeasurementsRepository()
    private val sessionRepository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
    private val TWENTY_FOUR_HOURS_MEASUREMENTS_COUNT = 24 * 60

    fun removeMeasurementsFromSessions() {
        // For next generations:
        // We want to remove measurements older than 24h,
        // but we treat 24 not like a date, but as a sum of measurements.
        // We know that we have 60 measurements per hour,
        // so we take 1440 last measurements for each stream in fixed sessions
        // and we remove older than the first of them.
        val fixedSessionsIds = sessionRepository.sessionsIdsByType(Session.Type.FIXED)
        val streamsForFixedSessionsIds = measurementStreamsRepository.getStreamsIdsBySessionIds(fixedSessionsIds)

        streamsForFixedSessionsIds.forEach { streamId ->
            val lastMeasurements = measurementRepository.getLastMeasurementsForStream(streamId, TWENTY_FOUR_HOURS_MEASUREMENTS_COUNT)
            if (shouldDeleteMeasurements(lastMeasurements.size)) {
                val lastExpectedMeasurement = lastMeasurements.last()
                val lastExpectedMeasurementTime: Date = lastExpectedMeasurement?.time!!
                measurementRepository.deleteMeasurementsOlderThan(streamId, lastExpectedMeasurementTime)
            }
        }
    }

    private fun shouldDeleteMeasurements(measurementsCount: Int): Boolean {
        // if measurementsCount is smaller than 1440 we don't want to remove anything
        return measurementsCount == TWENTY_FOUR_HOURS_MEASUREMENTS_COUNT
    }
}
