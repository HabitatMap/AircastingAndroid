package pl.llp.aircasting.sensor.airbeam3.sync

import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.data_classes.MeasurementDBObject
import pl.llp.aircasting.database.repositories.MeasurementStreamsRepository
import pl.llp.aircasting.database.repositories.MeasurementsRepository
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.sensor.airbeam3.sync.SDCardCSVFileFactory.Header
import java.io.File

class SDCardFixedSessionsProcessor(
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
        get() = mCSVFileFactory.getFixedFile()

    override fun run(deviceId: String, onFinishCallback: ((MutableList<Long>) -> Unit)?) {
        super.run(deviceId, null)
    }

    fun run(deviceId: String) {
        DatabaseProvider.runQuery {
            mSDCardCSVIterator.run(file).forEach { csvSession ->
                processSession(deviceId, csvSession)
            }
        }
    }

    override fun processSession(deviceId: String, csvSession: CSVSession?) {
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

    override fun filterMeasurements(
        sessionId: Long,
        measurementStreamId: Long,
        csvMeasurements: List<CSVMeasurement>
    ): List<CSVMeasurement> {
        val measurementsInDB =
            mMeasurementsRepository.getBySessionIdAndStreamId(sessionId, measurementStreamId)

        return csvMeasurements.filter { csvMeasurement ->
            isNotAlreadyInDB(csvMeasurement, measurementsInDB)
        }
    }

    private fun isNotAlreadyInDB(
        csvMeasurement: CSVMeasurement,
        measurementsInDB: List<MeasurementDBObject?>
    ): Boolean {
        val index = measurementsInDB.binarySearch{it?.time!!.compareTo(csvMeasurement.time)}

        return index < 0
    }
}
