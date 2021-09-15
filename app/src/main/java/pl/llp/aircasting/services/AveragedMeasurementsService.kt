package pl.llp.aircasting.services

import kotlinx.coroutines.runBlocking
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.repositories.MeasurementStreamsRepository
import pl.llp.aircasting.database.repositories.MeasurementsRepository
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.models.Measurement

class AveragedMeasurementsService {

        var averagingService: AveragingService? = null
        val sessionsRepository = SessionsRepository()
        var sessionId : Long? = 0
        val measurementsRepository = MeasurementsRepository()
        val streamRepository = MeasurementStreamsRepository()

        constructor(sessionUUID: String) {
            DatabaseProvider.runQuery {
                sessionId = sessionsRepository.getSessionIdByUUID(sessionUUID)
                if (sessionId != null) averagingService = AveragingService.get(sessionId!!)
                }
            }

        fun getMeasurementsOverSecondThreshold():  List<Measurement>?{ // TODO: debug this method!!
            if (sessionId == null) return emptyList()
            var measurements = listOf<Measurement>()
            DatabaseProvider.runQuery {
                val streams = streamRepository.getStreamsIdsBySessionId(sessionId!!)

                if (streams.isNotEmpty()) {
                    val measurementsDbObject = measurementsRepository.getLastMeasurementsForStream(streams.first(), 9)
                    measurements = measurementsDbObject.map { measurementDbObject ->
                        Measurement(measurementDbObject!!)
                    }
                }
            }
            return measurements
        }
}
