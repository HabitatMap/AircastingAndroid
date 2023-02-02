package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session

class SDCardMobileSessionsProcessor(
    mSDCardCSVIterator: SDCardSessionFileHandler,
    mSessionsRepository: SessionsRepository,
    mMeasurementStreamsRepository: MeasurementStreamsRepository,
    mMeasurementsRepository: MeasurementsRepositoryImpl
) : SDCardSessionsProcessor(
    mSDCardCSVIterator,
    mSessionsRepository,
    mMeasurementStreamsRepository,
    mMeasurementsRepository
) {
    // driver method to process CSVSession derived from line
    override fun processSession(deviceId: String, csvSession: CSVSession?) {
        csvSession?.uuid ?: return

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
}
