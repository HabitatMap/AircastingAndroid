package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.util.extensions.runOnIOThread
import java.io.File

abstract class SDCardSessionsProcessor(
    val mCSVFileFactory: SDCardCSVFileFactory,
    val mSDCardCSVIterator: SDCardCSVIterator,
    val mSessionsRepository: SessionsRepository,
    private val mMeasurementStreamsRepository: MeasurementStreamsRepository,
    val mMeasurementsRepository: MeasurementsRepositoryImpl
) {
    abstract val file: File
    val mProcessedSessionsIds: MutableList<Long> = mutableListOf()

    open fun run(deviceId: String, onFinishCallback: ((MutableList<Long>) -> Unit)? = null) {
        runOnIOThread {
            mSDCardCSVIterator.run(file).forEach { csvSession ->
                processSession(deviceId, csvSession)
            }

            onFinishCallback?.invoke(mProcessedSessionsIds)
        }
    }

    abstract fun processSession(deviceId: String, csvSession: CSVSession?)

    fun processMeasurements(deviceId: String, sessionId: Long, streamHeaderValue: Int, csvMeasurements: List<CSVMeasurement>) {
        val streamHeader = SDCardCSVFileFactory.Header.fromInt(streamHeaderValue)
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

    abstract fun filterMeasurements(
        sessionId: Long,
        measurementStreamId: Long,
        csvMeasurements: List<CSVMeasurement>
    ) : List<CSVMeasurement>
}