package pl.llp.aircasting.services

import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.repositories.MeasurementStreamsRepository
import pl.llp.aircasting.database.repositories.MeasurementsRepository
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.models.Measurement
import pl.llp.aircasting.models.MeasurementStream
import pl.llp.aircasting.screens.dashboard.charts.ChartAveragesCreator

class AveragedMeasurementsService(sessionUUID: String) {
        var averagingService: AveragingService? = null
        val sessionsRepository = SessionsRepository()
        var sessionId : Long? = 0
        val measurementsRepository = MeasurementsRepository()
        val streamRepository = MeasurementStreamsRepository()

    init {
        DatabaseProvider.runQuery {
            sessionId = sessionsRepository.getSessionIdByUUID(sessionUUID)
            if (sessionId != null) averagingService = AveragingService.get(sessionId!!)
        }
    }

        fun getMeasurementsOverSecondThreshold(stream: MeasurementStream): List<Measurement> {
            if (sessionId == null) return emptyList()
            var measurements = listOf<Measurement>()
            DatabaseProvider.runQuery {
                val streamId = streamRepository.getId(sessionId!!, stream)

                if (streamId != null) {
                    val measurementsDbObject = measurementsRepository.getLastMeasurementsWithGivenAveragingFrequency(
                        streamId,
                        ChartAveragesCreator.MAX_AVERAGES_AMOUNT,
                        AveragingService.SECOND_THRESHOLD_FREQUENCY)
                    measurements = measurementsDbObject.map { measurementDbObject ->
                        Measurement(measurementDbObject!!)
                    }
                }
            }
            return measurements
        }
}