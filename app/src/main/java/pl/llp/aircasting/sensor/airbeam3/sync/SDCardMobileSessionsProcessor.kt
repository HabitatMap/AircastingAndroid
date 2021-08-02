package pl.llp.aircasting.sensor.airbeam3.sync

import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.repositories.MeasurementStreamsRepository
import pl.llp.aircasting.database.repositories.MeasurementsRepository
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.sensor.airbeam3.sync.SDCardCSVFileFactory.Header

class SDCardMobileSessionsProcessor(
    private val mCSVFileFactory: SDCardCSVFileFactory,
    private val mSDCardCSVIterator: SDCardCSVIterator,
    private val mSessionsRepository: SessionsRepository,
    private val mMeasurementStreamsRepository: MeasurementStreamsRepository,
    private val mMeasurementsRepository: MeasurementsRepository
) {
    private val mProcessedSessionsIds: MutableList<Long> = mutableListOf()

    fun run(deviceId: String, onFinishCallback: (MutableList<Long>) -> Unit) {
        val file = mCSVFileFactory.getMobileFile()

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

    private fun processMeasurements(deviceId: String, sessionId: Long, streamHeaderValue: Int, csvMeasurements: List<CSVMeasurement>) {
        val streamHeader = Header.fromInt(streamHeaderValue)
        val csvMeasurementStream = CSVMeasurementStream.fromHeader(
            streamHeader
        ) ?: return

        val measurementStream = csvMeasurementStream.toMeasurementStream(deviceId)
        val measurementStreamId = mMeasurementStreamsRepository.getIdOrInsert(
            sessionId,
            measurementStream
        )

        // filtering measurements to save only the once we don't already have
        val filteredCSVMeasurements = filterMeasurements(sessionId, measurementStreamId, csvMeasurements)
        val measurements = filteredCSVMeasurements.map { csvMeasurement -> csvMeasurement.toMeasurement() }
        mMeasurementsRepository.insertAll(measurementStreamId, sessionId, measurements)
    }

    private fun filterMeasurements(sessionId: Long, measurementStreamId: Long, csvMeasurements: List<CSVMeasurement>): List<CSVMeasurement> {
        val lastMeasurementTime = mMeasurementsRepository.lastMeasurementTime(sessionId, measurementStreamId) ?: return csvMeasurements

        return csvMeasurements.filter { csvMeasurement -> csvMeasurement.time > lastMeasurementTime }
    }

    private fun finishSession(sessionId: Long, session: Session) {
        val lastMeasurementTime = mMeasurementsRepository.lastMeasurementTime(sessionId)
        session.stopRecording(lastMeasurementTime)
        mSessionsRepository.update(session)
    }
}
