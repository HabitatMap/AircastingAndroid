package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import android.util.Log
import pl.llp.aircasting.data.api.util.TAG
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
    override fun processSession(deviceId: String, csvSession: CSVSession?) {
        csvSession?.uuid ?: return

        val dbSession = mSessionsRepository.getSessionByUUID(csvSession.uuid)
        val session: Session?
        val sessionId: Long

        if (dbSession == null) {
            Log.v(TAG, "Could not find session with uuid: ${csvSession.uuid} in DB")
            session = csvSession.toSession(deviceId) ?: return
            sessionId = mSessionsRepository.insert(session)
        } else {
            session = Session(dbSession)
            sessionId = dbSession.id
        }

        Log.v(TAG, "Will save measurements: ${session.isDisconnected()}")
        if (session.isDisconnected()) {
            csvSession.streams.forEach { (headerKey, csvMeasurements) ->
                processMeasurements(deviceId, sessionId, headerKey, csvMeasurements)
                Log.v(TAG, "Processed stream: ${CSVMeasurementStream.SUPPORTED_STREAMS.keys.find { it.value == headerKey }}")
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
