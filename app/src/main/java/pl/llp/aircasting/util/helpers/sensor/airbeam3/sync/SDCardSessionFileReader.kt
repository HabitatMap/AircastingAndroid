package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import kotlinx.coroutines.*
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
    private val sessionRepository: SessionsRepository,
    private val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val defaultScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : SDCardSessionFileReader {
    override suspend fun read(file: File): CSVSession? = try {
        val lines = file.readLines()

        val sessionUUID = CSVSession.uuidFrom(lines.firstOrNull())
        val csvSession = CSVSession(sessionUUID)

        val dbSession = sessionRepository.getSessionByUUID(sessionUUID)
        val firstMeasurementTime = dbSession?.startTime
                ?: CSVSession.timestampFrom(lines.firstOrNull())
        val lastMeasurementTime = CSVSession.timestampFrom(lines.lastOrNull())
        val averagingFrequency =
            AveragingService.getAveragingFrequency(firstMeasurementTime, lastMeasurementTime)

        val averageExistingMeasurementsJob = ioScope.launch {
            AveragingService.get(dbSession?.id)
                ?.performFinalAveragingAfterSDSync(averagingFrequency)
        }

        val averageFileMeasurementsJob = defaultScope.launch {
            lines.chunked(averagingFrequency) { chunk ->
                // We do not include leftover measurements
                if (chunk.size < averagingFrequency) return@chunked

                val averageMeasurement = middleMeasurement(chunk)
                csvSession.addMeasurements(averageMeasurement)
            }
        }

        joinAll(averageExistingMeasurementsJob, averageFileMeasurementsJob)
        ioScope.cancel()
        defaultScope.cancel()

        csvSession
    } catch (e: IOException) {
        mErrorHandler.handle(SDCardMeasurementsParsingError(e))

        null
    }

    private fun middleMeasurement(chunk: List<String>) = chunk[chunk.size / 2]
}
