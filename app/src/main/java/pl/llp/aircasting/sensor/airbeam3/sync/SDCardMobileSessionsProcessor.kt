package pl.llp.aircasting.sensor.airbeam3.sync

import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.repositories.MeasurementStreamsRepository
import pl.llp.aircasting.database.repositories.MeasurementsRepository
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.sensor.airbeam3.sync.SDCardCSVFileFactory.Header
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

        DatabaseProvider.runQuery {
            mSDCardCSVIterator.run(file).forEach { csvSession ->
                processSession(deviceId, csvSession)
            }

            onFinishCallback.invoke(mProcessedSessionsIds)
        }
    }

    private fun processSession(deviceId: String, csvSession: CSVSession?) {
        csvSession ?: return

        val dbSession = mSessionsRepository.getSessionByUUID(csvSession.uuid)
        val session: Session?
        val sessionId: Long

        if (dbSession == null) {
            session = csvSession.toSession(deviceId) ?: return
            sessionId = mSessionsRepository.insert(session)
        } else {
            session = Session(dbSession)
            sessionId = dbSession.id
        }

        if (session.isDisconnected()) {
            csvSession.streams.forEach { (headerKey, csvMeasurements) ->
                processMeasurements(deviceId, sessionId, headerKey, csvMeasurements)
            }

            finishSession(sessionId, session)
            mProcessedSessionsIds.add(sessionId)
        }
    }

    private fun finishSession(sessionId: Long, session: Session) {
        val lastMeasurementTime = mMeasurementsRepository.lastMeasurementTime(sessionId)
        session.stopRecording(lastMeasurementTime)
        mSessionsRepository.update(session)
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
