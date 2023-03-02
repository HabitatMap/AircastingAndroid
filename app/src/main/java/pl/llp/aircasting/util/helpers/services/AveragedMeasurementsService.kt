package pl.llp.aircasting.util.helpers.services

import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.ui.view.screens.dashboard.charts.ChartAveragesCreator
import pl.llp.aircasting.util.extensions.runOnIOThread

class AveragedMeasurementsService(sessionUUID: String) {
        val sessionsRepository = SessionsRepository()
        var sessionId : Long? = 0
        val measurementsRepository = MeasurementsRepositoryImpl()
        val streamRepository = MeasurementStreamsRepository()

    init {
        runOnIOThread {
            sessionId = sessionsRepository.getSessionIdByUUID(sessionUUID)
        }
    }

        fun getMeasurementsOverSecondThreshold(stream: MeasurementStream): List<Measurement> {
            if (sessionId == null) return emptyList()
            var measurements = listOf<Measurement>()
            runOnIOThread {
                val streamId = streamRepository.getId(sessionId!!, stream)

                if (streamId != null) {
                    val measurementsDbObject = measurementsRepository.getLastMeasurementsWithGivenAveragingFrequency(
                        streamId,
                        ChartAveragesCreator.MAX_AVERAGES_AMOUNT,
                        AveragingWindow.SECOND.value)
                    measurements = measurementsDbObject.map { measurementDbObject ->
                        Measurement(measurementDbObject!!)
                    }
                }
            }
            return measurements
        }
}
