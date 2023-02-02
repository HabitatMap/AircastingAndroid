package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.llp.aircasting.data.local.entity.MeasurementDBObject
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import java.io.File

abstract class SDCardSessionsProcessor(
    private val mSDCardSessionFileHandler: SDCardSessionFileHandler,
    val mSessionsRepository: SessionsRepository,
    private val mMeasurementStreamsRepository: MeasurementStreamsRepository,
    val mMeasurementsRepository: MeasurementsRepositoryImpl,
    protected val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val mProcessedSessionsIds: MutableList<Long> = mutableListOf()

    fun start(
        file: File,
        deviceId: String
    ) = coroutineScope.launch {
        val csvSession = mSDCardSessionFileHandler.handle(file)
        processSession(deviceId, csvSession)
    }

    abstract fun processSession(deviceId: String, csvSession: CSVSession?)

    fun processMeasurements(
        deviceId: String,
        sessionId: Long,
        streamHeaderValue: Int,
        csvMeasurements: List<CSVMeasurement>
    ) {
        val streamHeader = SDCardCSVFileFactory.Header.fromInt(streamHeaderValue)
        val csvMeasurementStream = CSVMeasurementStream.fromHeader(
            streamHeader
        ) ?: return

        val measurementStream = csvMeasurementStream.toMeasurementStream(deviceId)
        val measurementStreamId = mMeasurementStreamsRepository.getIdOrInsert(
            sessionId,
            measurementStream
        )

        // filtering measurements to save only the ones we don't already have
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

        return csvMeasurements.filter { csvMeasurement ->
            isNotAlreadyInDB(csvMeasurement, measurementsInDB)
        }
    }

    private fun isNotAlreadyInDB(
        csvMeasurement: CSVMeasurement,
        measurementsInDB: List<MeasurementDBObject?>
    ): Boolean {
        val index = measurementsInDB.binarySearch { it?.time?.compareTo(csvMeasurement.time) ?: -1 }
        return index < 0
    }
}