package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.models.Session

class RemoveOldMeasurementsService() {
    private val measurementRepository = MeasurementsRepository()
    private val sessionRepository = SessionsRepository()

    fun removeMeasurementsFromSessions() {
        val fixedSessionsIds = sessionRepository.sessionsIdsByType(Session.Type.FIXED)
        measurementRepository.deleteMeasurements(fixedSessionsIds)
    }
}
