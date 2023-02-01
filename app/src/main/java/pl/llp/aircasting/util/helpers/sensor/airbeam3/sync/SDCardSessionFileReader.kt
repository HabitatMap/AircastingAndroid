package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SDCardMeasurementsParsingError
import pl.llp.aircasting.util.helpers.services.AveragingService
import java.io.File
import java.io.IOException

interface SDCardSessionFileReader {
    suspend fun read(file: File): CSVSession?
}

class SDCardSessionFileReaderFixed(
    private val mErrorHandler: ErrorHandler
) : SDCardSessionFileReader {
    override suspend fun read(file: File): CSVSession? = try {
        val lines = file.readLines()
        val sessionUUID = CSVSession.uuidFrom(lines.firstOrNull())
        val csvSession = CSVSession(sessionUUID)
        lines.forEach { line ->
            csvSession.addMeasurements(line)
        }

        csvSession
    } catch (e: IOException) {
        mErrorHandler.handle(SDCardMeasurementsParsingError(e))

        null
    }
}

class SDCardSessionFileReaderMobile(
    private val mErrorHandler: ErrorHandler,
    private val sessionRepository: SessionsRepository
) : SDCardSessionFileReader {
    override suspend fun read(file: File): CSVSession? = try {
        val lines = file.readLines()

        val sessionUUID = CSVSession.uuidFrom(lines.firstOrNull())
        val csvSession = CSVSession(sessionUUID)

        val firstMeasurementTime = sessionRepository.getSessionStartTime(sessionUUID)
                ?: CSVSession.timestampFrom(lines.firstOrNull())
        val lastMeasurementTime = CSVSession.timestampFrom(lines.lastOrNull())
        val averagingThreshold =
            AveragingService.getAveragingThreshold(firstMeasurementTime, lastMeasurementTime)

        lines.chunked(averagingThreshold) { chunk ->
            // We do not include leftover measurements
            if (chunk.size < averagingThreshold) return@chunked

            val averageMeasurement = middleMeasurement(chunk)
            csvSession.addMeasurements(averageMeasurement)
        }

        csvSession
    } catch (e: IOException) {
        mErrorHandler.handle(SDCardMeasurementsParsingError(e))

        null
    }

    private fun middleMeasurement(chunk: List<String>) = chunk[chunk.size / 2]
}
