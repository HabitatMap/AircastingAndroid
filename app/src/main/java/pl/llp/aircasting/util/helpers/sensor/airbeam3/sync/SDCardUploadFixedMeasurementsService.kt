package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import android.annotation.SuppressLint
import android.util.Log
import pl.llp.aircasting.data.api.services.UploadFixedMeasurementsService
import java.io.File

class SDCardUploadFixedMeasurementsService(
    private val mSDCardCSVIterator: SDCardSessionFileHandlerFixed,
    private val mUploadFixedMeasurementsService: UploadFixedMeasurementsService?
) {
    private val MEASUREMENTS_CHUNK_SIZE = 31 * 24 * 60 // about a month of data
    private val TAG = "SDCardUploadFixedMeasurements"

    suspend fun start(file: File, deviceId: String) {
        val csvSession = mSDCardCSVIterator.handle(file)
        processSession(deviceId, csvSession)
    }

    private suspend fun processSession(deviceId: String, csvSession: CSVSession?) {
        csvSession ?: return

        val measurementChunks = chunkSession(csvSession)
        uploadSession(deviceId, csvSession.uuid, measurementChunks)
    }

    // chunking here is kind of complicated, but it's needed this way, otherwise streams got dupplicated in a backend
    private fun chunkSession(csvSession: CSVSession): Map<CSVMeasurementStream, ArrayList<List<CSVMeasurement>>> {
        val measurementChunks =
            mutableMapOf<CSVMeasurementStream, ArrayList<List<CSVMeasurement>>>()

        csvSession.streams.forEach { (streamHeaderValue, csvMeasurements) ->
            val streamLineParameter = CSVSession.LineParameter.fromInt(streamHeaderValue)
            val csvMeasurementStream = CSVSession.fromHeader(
                streamLineParameter
            )

            if (csvMeasurementStream != null) {
                val csvMeasurementsChunk = chunkStream(csvMeasurements)
                measurementChunks[csvMeasurementStream] = csvMeasurementsChunk
            }
        }

        return measurementChunks
    }

    @SuppressLint("LongLogTag")
    private suspend fun uploadSession(
        deviceId: String,
        sessionUUID: String?,
        allChunks: Map<CSVMeasurementStream, ArrayList<List<CSVMeasurement>>>
    ) {
        sessionUUID ?: return

        while (true) {
            val allStreamsChunks = allChunks.filter { (_, chunks) -> chunks.size > 0 }
            if (allStreamsChunks.isEmpty()) {
                Log.d(TAG, "Upload of all chunks queued.")
                break
            }

            Log.d(TAG, "Remaining chunks: ${allStreamsChunks.map { (stream, chunks) -> "${stream.sensorName}: ${chunks.size}" }}.")

            allStreamsChunks.forEach { (csvMeasurementStream, csvMeasurementsChunks) ->
                val csvMeasurementsChunk = csvMeasurementsChunks.removeAt(0)

                uploadMeasurementsChunk(
                    deviceId,
                    sessionUUID,
                    csvMeasurementStream,
                    csvMeasurementsChunk
                )
            }
        }
    }

    private fun chunkStream(csvMeasurements: List<CSVMeasurement>): ArrayList<List<CSVMeasurement>> {
        return ArrayList(csvMeasurements.chunked(MEASUREMENTS_CHUNK_SIZE).map { csvMeasurementsChunk ->
            csvMeasurementsChunk
        })
    }

    @SuppressLint("LongLogTag")
    private suspend fun uploadMeasurementsChunk(
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
            csvMeasurementsChunk
        )?.onSuccess {
            Log.d(TAG, "Successfully finished chunk upload for ${csvMeasurementStream.sensorName}.")
        }?.onFailure {
            Log.d(TAG, "Error while uploading chunk for ${csvMeasurementStream.sensorName}.")
        }
    }
}
