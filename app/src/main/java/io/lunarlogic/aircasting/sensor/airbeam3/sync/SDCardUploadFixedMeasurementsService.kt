package io.lunarlogic.aircasting.sensor.airbeam3.sync

import android.annotation.SuppressLint
import android.util.Log
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.networking.services.UploadFixedMeasurementsService

class SDCardUploadFixedMeasurementsService(
    private val mSDCardCSVFileFactory: SDCardCSVFileFactory,
    private val mSDCardCSVIterator: SDCardCSVIterator,
    private val mUploadFixedMeasurementsService: UploadFixedMeasurementsService
) {
    private val MEASUREMENTS_CHUNK_SIZE = 31 * 24 * 60 // about a month of data
    private val TAG = "SDCardUploadFixedMeasurements"
    private var processedChunksCount = 0

    fun run() {
        processedChunksCount = 0

        DatabaseProvider.runQuery {
            val file = mSDCardCSVFileFactory.getFixed()
            val deviceId = "246f28c47698" // TODO: move it to the file name

            mSDCardCSVIterator.run(file).forEach { csvSession ->
                processSession(deviceId, csvSession)
            }
        }
    }

    private fun processSession(deviceId: String, csvSession: CSVSession?) {
        csvSession ?: return

        csvSession.streams.forEach { (streamHeaderValue, csvMeasurements) ->
            val streamHeader = SDCardCSVFileFactory.Header.fromInt(streamHeaderValue)
            val csvMeasurementStream = CSVMeasurementStream.fromHeader(
                streamHeader
            )

            uploadStream(deviceId, csvSession, csvMeasurementStream, csvMeasurements)
        }
    }

    private fun uploadStream(
        deviceId: String,
        csvSession: CSVSession,
        csvMeasurementStream: CSVMeasurementStream?,
        csvMeasurements: List<CSVMeasurement>
    ) {
        csvMeasurementStream ?: return

        csvMeasurements.chunked(MEASUREMENTS_CHUNK_SIZE).forEach { csvMeasurementsChunk ->
            processMeasurementsChunk(deviceId, csvSession, csvMeasurementStream, csvMeasurementsChunk)
        }
    }

    @SuppressLint("LongLogTag")
    private fun processMeasurementsChunk(
        deviceId: String,
        csvSession: CSVSession,
        csvMeasurementStream: CSVMeasurementStream,
        csvMeasurementsChunk: List<CSVMeasurement>
    ) {
        if (csvMeasurementsChunk.isEmpty()) return

        processedChunksCount += csvMeasurementsChunk.size

        Log.d(TAG, "Already processed chunk count: ${processedChunksCount}.")
        Log.d(TAG, "Now processing ${csvMeasurementStream.sensorName}...")
        mUploadFixedMeasurementsService.upload(
            csvSession.uuid,
            deviceId,
            csvMeasurementStream,
            csvMeasurementsChunk,
            { println("ANIA SUCCESS!") },
            { println("ANIA ERROR :(") })
    }
}
