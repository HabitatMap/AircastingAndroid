package pl.llp.aircasting.util.helpers.services

import kotlinx.coroutines.runBlocking
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.ui.view.screens.dashboard.charts.ChartAveragesCreator

// TODO(25/03/23):
//  Leaving for backward compatibility, but potentially this is not needed - as measurements should
//  already be averaged, and we can just pull all of them

class AveragedMeasurementsService(
    private val sessionUUID: String,
    val sessionsRepository: SessionsRepository = SessionsRepository(),
    val measurementsRepository: MeasurementsRepositoryImpl = MeasurementsRepositoryImpl(),
    private val streamRepository: MeasurementStreamsRepository = MeasurementStreamsRepository(),
) {
    fun getMeasurementsOverSecondThreshold(stream: MeasurementStream): List<Measurement> =
        runBlocking {
            val sessionId = sessionsRepository.getSessionIdByUUIDSuspend(sessionUUID)
                ?: return@runBlocking emptyList()
            val streamId =
                streamRepository.getId(sessionId, stream) ?: return@runBlocking emptyList()

            val measurementsDbObject =
                measurementsRepository.getLastMeasurementsWithGivenAveragingFrequency(
                    streamId,
                    ChartAveragesCreator.MAX_AVERAGES_AMOUNT,
                    AveragingWindow.SECOND.value
                )

            return@runBlocking measurementsDbObject.map { measurementDbObject ->
                Measurement(measurementDbObject!!)
            }
        }
}
