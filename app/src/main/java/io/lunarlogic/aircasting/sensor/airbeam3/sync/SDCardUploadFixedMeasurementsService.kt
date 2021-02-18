package io.lunarlogic.aircasting.sensor.airbeam3.sync

import android.annotation.SuppressLint
import android.util.Log
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.networking.services.UploadFixedMeasurementsService

class SDCardUploadFixedMeasurementsService(
    private val mSDCardCSVFileFactory: SDCardCSVFileFactory,
    private val mSDCardCSVIterator: SDCardCSVIterator,
    private val mUploadFixedMeasurementsService: UploadFixedMeasurementsService?
) {
    private val MEASUREMENTS_CHUNK_SIZE = 31 * 24 * 60 // about a month of data
    private val TAG = "SDCardUploadFixedMeasurements"

    fun run(deviceId: String, onFinishCallback: () -> Unit) {
        DatabaseProvider.runQuery {
            val file = mSDCardCSVFileFactory.getFixedFile()

            mSDCardCSVIterator.run(file).forEach { csvSession ->
                processSession(deviceId, csvSession)
            }

            onFinishCallback.invoke()
        }
    }

    private fun processSession(deviceId: String, csvSession: CSVSession?) {
        csvSession ?: return

        val measurementChunks = chunkSession(csvSession)
        uploadSession(deviceId, csvSession.uuid, measurementChunks)
    }

    private fun chunkSession(csvSession: CSVSession): Map<CSVMeasurementStream, ArrayList<List<CSVMeasurement>>> {
        val measurementChunks = mutableMapOf<CSVMeasurementStream, ArrayList<List<CSVMeasurement>>>()

        csvSession.streams.forEach { (streamHeaderValue, csvMeasurements) ->
            val streamHeader = SDCardCSVFileFactory.Header.fromInt(streamHeaderValue)
            val csvMeasurementStream = CSVMeasurementStream.fromHeader(
                streamHeader
            )

            if (csvMeasurementStream != null) {
                val csvMeasurementsChunk = chunkStream(csvMeasurements)
                measurementChunks[csvMeasurementStream] = csvMeasurementsChunk
            }
        }

        return measurementChunks
    }

    @SuppressLint("LongLogTag")
    private fun uploadSession(deviceId: String, sessionUUID: String, allChunks: Map<CSVMeasurementStream, ArrayList<List<CSVMeasurement>>>) {
        while (true) {
            val allStreamsChunks = allChunks.filter { (_, chunks) -> chunks.size > 0 }
            if (allStreamsChunks.isEmpty()) {
                Log.d(TAG, "Upload of all chunks queued.")
                break
            }

            Log.d(TAG, "Remaining chunks: ${allStreamsChunks.map { (stream, chunks) -> "${stream.sensorName}: ${chunks.size}" }}.")

            allStreamsChunks.forEach { (csvMeasurementStream, csvMeasurementsChunks) ->
                val csvMeasurementsChunk = csvMeasurementsChunks.removeAt(0)

                uploadMeasurementsChunk(deviceId, sessionUUID, csvMeasurementStream, csvMeasurementsChunk)
            }
        }
    }

    private fun chunkStream(csvMeasurements: List<CSVMeasurement>): ArrayList<List<CSVMeasurement>> {
        return ArrayList(csvMeasurements.chunked(MEASUREMENTS_CHUNK_SIZE).map { csvMeasurementsChunk ->
            csvMeasurementsChunk
        })
    }

    @SuppressLint("LongLogTag")
    private fun uploadMeasurementsChunk(
        deviceId: String,
        sessionUUID: String,
        csvMeasurementStream: CSVMeasurementStream,
        csvMeasurementsChunk: List<CSVMeasurement>
    ) {
        if (csvMeasurementsChunk.isEmpty()) return

        Log.d(TAG, "Now processing ${csvMeasurementStream.sensorName}...")
        mUploadFixedMeasurementsService?.upload(
            sessionUUID,
            deviceId,
            csvMeasurementStream,
            csvMeasurementsChunk,
            { Log.d(TAG, "Successfully finished chunk upload for ${csvMeasurementStream.sensorName}.") },
            { Log.d(TAG, "Error while uploading chunk for ${csvMeasurementStream.sensorName}.") })
    }
}
