package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.models.Session
import java.util.*

class RemoveOldMeasurementsService() {
    private val measurementRepository = MeasurementsRepository()
    private val sessionRepository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
    private val TWENTY_FOUR_HOURS_MEASUREMENTS_COUNT = 24 * 60

    fun removeMeasurementsFromSessions() {
        val fixedSessionsIds = sessionRepository.sessionsIdsByType(Session.Type.FIXED)
        fixedSessionsIds.forEach {fixedSessionId ->
            val lastMeasurements = measurementRepository.getLastMeasurements(fixedSessionId, TWENTY_FOUR_HOURS_MEASUREMENTS_COUNT)
            if (shouldDeleteMeasurements(lastMeasurements.size)) {
                val lastExpectedMeasurement = lastMeasurements.last()
                val lastExpectedMeasurementTime: Date = lastExpectedMeasurement?.time!!
                measurementRepository.deleteMeasurementsOlderThen(fixedSessionId, lastExpectedMeasurementTime)
            }
        }
    }

    private fun shouldDeleteMeasurements(measurementsCount: Int): Boolean {
        return measurementsCount == TWENTY_FOUR_HOURS_MEASUREMENTS_COUNT
    }
}
