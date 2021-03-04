package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.models.Session
import java.util.*

class RemoveOldMeasurementsService() {
    private val measurementRepository = MeasurementsRepository()
    private val sessionRepository = SessionsRepository()
    private val TWENTY_FOUR_HOURS_MEASUREMENTS_COUNT = 1440

    fun removeMeasurementsFromSessions() {
        val fixedSessionsIds = sessionRepository.sessionsIdsByType(Session.Type.FIXED)
        val lastMeasurements = measurementRepository.getLastMeasurements(fixedSessionsIds, TWENTY_FOUR_HOURS_MEASUREMENTS_COUNT)
        if (checkIfDeleteMeasurements(lastMeasurements.size)) {
            val lastExpectedMeasurement = lastMeasurements.last()
            val lastExpectedMeasurementTime: Date = lastExpectedMeasurement?.time!!
            measurementRepository.deleteMeasurements(fixedSessionsIds, lastExpectedMeasurementTime)
        }
    }

    private fun checkIfDeleteMeasurements(measurementsCount: Int): Boolean {
        return measurementsCount == TWENTY_FOUR_HOURS_MEASUREMENTS_COUNT
    }
}
