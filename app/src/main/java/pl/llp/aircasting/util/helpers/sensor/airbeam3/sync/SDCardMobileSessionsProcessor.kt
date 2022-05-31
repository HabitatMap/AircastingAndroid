package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.LocalSession
import java.io.File

class SDCardMobileSessionsProcessor(
    mCSVFileFactory: SDCardCSVFileFactory,
    mSDCardCSVIterator: SDCardCSVIterator,
    mSessionsRepository: SessionsRepository,
    mMeasurementStreamsRepository: MeasurementStreamsRepository,
    mMeasurementsRepository: MeasurementsRepository
) : SDCardSessionsProcessor(
    mCSVFileFactory,
    mSDCardCSVIterator,
    mSessionsRepository,
    mMeasurementStreamsRepository,
    mMeasurementsRepository
) {
    override val file: File
        get() = mCSVFileFactory.getMobileFile()

    // TODO: Discuss with someone how to derive base implementation of the method in SDCardSessionsProcessor
    override fun processSession(deviceId: String, csvSession: CSVSession?) {
        csvSession ?: return

        val dbSession = mSessionsRepository.getSessionByUUID(csvSession.uuid)
        val localSession: LocalSession?
        val sessionId: Long

        if (dbSession == null) {
            localSession = csvSession.toSession(deviceId) ?: return
            sessionId = mSessionsRepository.insert(localSession)
        } else {
            localSession = LocalSession(dbSession)
            sessionId = dbSession.id
        }

        if (localSession.isDisconnected()) {
            csvSession.streams.forEach { (headerKey, csvMeasurements) ->
                processMeasurements(deviceId, sessionId, headerKey, csvMeasurements)
            }

            finishSession(sessionId, localSession)
            mProcessedSessionsIds.add(sessionId)
        }
    }

    private fun finishSession(sessionId: Long, localSession: LocalSession) {
        val lastMeasurementTime = mMeasurementsRepository.lastMeasurementTime(sessionId)
        localSession.stopRecording(lastMeasurementTime)
        mSessionsRepository.update(localSession)
    }

    override fun filterMeasurements(
        sessionId: Long,
        measurementStreamId: Long,
        csvMeasurements: List<CSVMeasurement>
    ): List<CSVMeasurement> {
        val lastMeasurementTime =
            mMeasurementsRepository.lastMeasurementTime(sessionId, measurementStreamId)
                ?: return csvMeasurements

        return csvMeasurements.filter { csvMeasurement -> csvMeasurement.time > lastMeasurementTime }
    }
}
