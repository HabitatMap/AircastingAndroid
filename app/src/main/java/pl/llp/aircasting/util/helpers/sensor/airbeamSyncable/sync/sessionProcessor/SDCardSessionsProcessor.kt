package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor

import android.util.Log
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSessionFileHandler
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.CSVMeasurement
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.CSVSession
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.lineParameter.CSVLineParameterHandler
import java.io.File

abstract class SDCardSessionsProcessor(
    private val mSDCardSessionFileHandler: SDCardSessionFileHandler,
    val mSessionsRepository: SessionsRepository,
    val mMeasurementStreamsRepository: MeasurementStreamsRepository,
    val mMeasurementsRepository: MeasurementsRepositoryImpl,
    private val lineParameterHandler: CSVLineParameterHandler,
) {
    suspend fun start(
        file: File,
        deviceId: String
    ) {
        val csvSession = mSDCardSessionFileHandler.handle(file)
        Log.v(TAG, "Finished SDCardSessionFileHandler with resulting csvSession: $csvSession")
        processSession(deviceId, csvSession)
    }

    abstract suspend fun processSession(deviceId: String, csvSession: CSVSession?)

    suspend fun processMeasurements(
        deviceId: String,
        sessionId: Long,
        streamLineParameter: CSVLineParameterHandler.ABLineParameter,
        csvMeasurements: List<CSVMeasurement>
    ) {
        val csvMeasurementStream = lineParameterHandler.csvStreamByLineParameter(
            streamLineParameter
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

        Log.v(
            TAG,
            "Inserting ${measurements.count()} measurements from ${measurementStream.sensorName}"
        )

        mMeasurementsRepository.insertAll(measurementStreamId, sessionId, measurements)
    }

    abstract suspend fun filterMeasurements(
        sessionId: Long,
        measurementStreamId: Long,
        csvMeasurements: List<CSVMeasurement>
    ): List<CSVMeasurement>
}