package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository

class RemoveOldMeasurementsService() {
    private val measurementRepository = MeasurementsRepository()

    fun removeMeasurementsFromSessions(fixedSessionsIds: List<Long>) {
        measurementRepository.deleteMeasurements(fixedSessionsIds)
    }
}
