package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository

class SDCardFixedSessionsProcessor(
    mCSVFileFactory: SDCardCSVFileFactory,
    mSDCardCSVIterator: SDCardCSVIteratorFixed,
    mSessionsRepository: SessionsRepository,
    mMeasurementStreamsRepository: MeasurementStreamsRepository,
    mMeasurementsRepository: MeasurementsRepositoryImpl
) : SDCardSessionsProcessor(
    mCSVFileFactory,
    mSDCardCSVIterator,
    mSessionsRepository,
    mMeasurementStreamsRepository,
    mMeasurementsRepository
) {
    override fun processSession(deviceId: String, csvSession: CSVSession?) {
        csvSession ?: return

        val dbSession = mSessionsRepository.getSessionByUUID(csvSession.uuid)
        val sessionId: Long? = dbSession?.id

        sessionId ?: return

        csvSession.streams.forEach { (headerKey, csvMeasurements) ->
            processMeasurements(deviceId, sessionId, headerKey, csvMeasurements)
        }
    }
}
