package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import pl.llp.aircasting.data.local.entity.MeasurementDBObject
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.util.extensions.runOnIOThread
import java.io.File

class SDCardFixedSessionsProcessor(
    mCSVFileFactory: SDCardCSVFileFactory,
    mSDCardCSVIterator: SDCardCSVIterator,
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
    override val file: File
        get() = mCSVFileFactory.getFixedFile()

    override fun run(deviceId: String, onFinishCallback: ((MutableList<Long>) -> Unit)?) {
        super.run(deviceId, null)
    }

    fun run(deviceId: String) {
        runOnIOThread {
            mSDCardCSVIterator.run(file).forEach { csvSession ->
                processSession(deviceId, csvSession)
            }
        }
    }

    override fun processSession(deviceId: String, csvSession: CSVSession?) {
        csvSession ?: return

        val dbSession = mSessionsRepository.getSessionByUUID(csvSession.uuid)
        val sessionId: Long? = dbSession?.id

        sessionId ?: return

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
        val index = measurementsInDB.binarySearch{it?.time?.compareTo(csvMeasurement.time) ?: -1}
        return index < 0
    }
}
