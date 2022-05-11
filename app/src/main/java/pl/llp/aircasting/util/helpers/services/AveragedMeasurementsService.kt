package pl.llp.aircasting.util.helpers.services

import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.api.repositories.MeasurementStreamsRepository
import pl.llp.aircasting.data.api.repositories.MeasurementsRepository
import pl.llp.aircasting.data.api.repositories.SessionsRepository
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.ui.view.screens.dashboard.charts.ChartAveragesCreator

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
