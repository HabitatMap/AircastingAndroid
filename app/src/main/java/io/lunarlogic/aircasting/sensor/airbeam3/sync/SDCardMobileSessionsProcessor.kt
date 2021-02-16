package io.lunarlogic.aircasting.sensor.airbeam3.sync

import io.lunarlogic.aircasting.sensor.airbeam3.sync.SDCardCSVFileFactory.Header
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.models.Session

class SDCardMobileSessionsProcessor(
    private val mCSVFileFactory: SDCardCSVFileFactory,
    private val mSDCardCSVIterator: SDCardCSVIterator,
    private val mSessionsRepository: SessionsRepository,
    private val mMeasurementStreamsRepository: MeasurementStreamsRepository,
    private val mMeasurementsRepository: MeasurementsRepository
) {
    // TODO: move deviceId to the file name
    fun run(deviceId: String, onFinishCallback: () -> Unit) {
        val file = mCSVFileFactory.getMobileFile()

        DatabaseProvider.runQuery {
            mSDCardCSVIterator.run(file).forEach { csvSession ->
                processSession(deviceId, csvSession)
            }

            onFinishCallback.invoke()
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

            finishSession(session)
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

        val filteredCSVMeasurements = filterMeasurements(sessionId, measurementStreamId, csvMeasurements)
        val measurements = filteredCSVMeasurements.map { csvMeasurement -> csvMeasurement.toMeasurement() }
        mMeasurementsRepository.insertAll(measurementStreamId, sessionId, measurements)
    }

    private fun filterMeasurements(sessionId: Long, measurementStreamId: Long, csvMeasurements: List<CSVMeasurement>): List<CSVMeasurement> {
        val lastMeasurementTime = mMeasurementsRepository.lastMeasurementTime(sessionId, measurementStreamId) ?: return csvMeasurements

        return csvMeasurements.filter { csvMeasurement -> csvMeasurement.time > lastMeasurementTime }
    }

    private fun finishSession(session: Session) {
        session.stopRecording()
        mSessionsRepository.update(session)
    }
}
