package pl.llp.aircasting.sensor.airbeam3.sync

import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.data_classes.MeasurementDBObject
import pl.llp.aircasting.database.repositories.MeasurementStreamsRepository
import pl.llp.aircasting.database.repositories.MeasurementsRepository
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.sensor.airbeam3.sync.SDCardCSVFileFactory.Header

class SDCardFixedSessionsProcessor(
    private val mCSVFileFactory: SDCardCSVFileFactory,
    private val mSDCardCSVIterator: SDCardCSVIterator,
    private val mSessionsRepository: SessionsRepository,
    private val mMeasurementStreamsRepository: MeasurementStreamsRepository,
    private val mMeasurementsRepository: MeasurementsRepository
) {
    fun run(deviceId: String) {
        val file = mCSVFileFactory.getFixedFile()

        DatabaseProvider.runQuery {
            mSDCardCSVIterator.run(file).forEach { csvSession ->
                processSession(deviceId, csvSession)
            }
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
            sessionId = dbSession.id
        }

        csvSession.streams.forEach { (headerKey, csvMeasurements) ->
            processMeasurements(deviceId, sessionId, headerKey, csvMeasurements)
        }
    }

    private fun processMeasurements(
        deviceId: String,
        sessionId: Long,
        streamHeaderValue: Int,
        csvMeasurements: List<CSVMeasurement>
    ) {
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
        val filteredCSVMeasurements =
            filterMeasurements(sessionId, measurementStreamId, csvMeasurements)
        val measurements =
            filteredCSVMeasurements.map { csvMeasurement -> csvMeasurement.toMeasurement() }
        mMeasurementsRepository.insertAll(measurementStreamId, sessionId, measurements)
    }

    private fun filterMeasurements(
        sessionId: Long,
        measurementStreamId: Long,
        csvMeasurements: List<CSVMeasurement>
    ): List<CSVMeasurement> {
        val measurementsInDB =
            mMeasurementsRepository.getBySessionIdAndStreamId(sessionId, measurementStreamId)

        // TODO: Should check if all the measurements not present in table
        // TODO: There should be a faster way of dealing with it
        // e.g.: binary search in sorted list
        return csvMeasurements.filter { csvMeasurement ->
            isNotAlreadyInDB(csvMeasurement, measurementsInDB)
        }
    }

    private fun isNotAlreadyInDB(
        csvMeasurement: CSVMeasurement,
        measurementsInDB: List<MeasurementDBObject?>
    ): Boolean {
        var noDuplicate = true
        for (measurement in measurementsInDB) {
            if (measurement?.time == csvMeasurement.time) noDuplicate = false
        }
        return noDuplicate
    }
}
