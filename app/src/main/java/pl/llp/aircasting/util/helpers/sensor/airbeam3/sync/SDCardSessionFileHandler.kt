package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import kotlinx.coroutines.*
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SDCardMeasurementsParsingError
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardCSVFileFactory.Companion.AB_DELIMITER
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardCSVFileFactory.Companion.airBeamParams
import pl.llp.aircasting.util.helpers.services.AveragingService
import java.io.File
import java.io.IOException

interface SDCardSessionFileHandler {
    suspend fun handle(file: File): CSVSession?
}

class SDCardSessionFileHandlerFixed(
    private val mErrorHandler: ErrorHandler
) : SDCardSessionFileHandler {
    override suspend fun handle(file: File): CSVSession? = try {
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

class SDCardSessionFileHandlerMobile(
    private val mErrorHandler: ErrorHandler,
    private val sessionRepository: SessionsRepository,
    private val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val defaultScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : SDCardSessionFileHandler {
    override suspend fun handle(file: File): CSVSession? = try {
        val lines = file.readLines()
        val sessionUUID = CSVSession.uuidFrom(lines.firstOrNull())
        val csvSession = CSVSession(sessionUUID)

        val dbSession = sessionRepository.getSessionByUUID(sessionUUID)
        val averagingFrequency = getFinalAveragingFrequency(dbSession, lines)

        val averageExistingMeasurementsJob = ioScope.launch {
            AveragingService.get(dbSession?.id)
                ?.performFinalAveragingAfterSDSync(averagingFrequency)
        }

        val averageFileMeasurementsJob = defaultScope.launch {
            lines.chunked(averagingFrequency) { chunk ->
                // We do not include leftover measurements
                if (chunk.size < averagingFrequency) return@chunked

                val averageMeasurement = averageMeasurementFrom(chunk)
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

    private fun getFinalAveragingFrequency(
        dbSession: SessionDBObject?,
        lines: List<String>
    ): Int {
        val firstMeasurementTime = dbSession?.startTime
            ?: CSVSession.timestampFrom(lines.firstOrNull())
        val lastMeasurementTime = CSVSession.timestampFrom(lines.lastOrNull())
        return AveragingService.getAveragingFrequency(firstMeasurementTime, lastMeasurementTime)
    }

    private fun averageMeasurementFrom(chunk: List<String>): String {
        val middleMeasurement = chunk[chunk.size / 2]
        val middleMeasurementParams = middleMeasurement.airBeamParams()
        val lineWithAveragedValuesParameters = middleMeasurementParams.toMutableList()
        CSVMeasurementStream.SUPPORTED_STREAMS.keys.forEach { currentStreamHeader ->
            var sumOfHeaderValuesInChunk = 0.0
            var countOfNonNullMeasurements = 0
            chunk.forEach { line ->
                val params = line.airBeamParams()
                val streamValue = params[currentStreamHeader.value].toDoubleOrNull()
                if (streamValue != null) {
                    sumOfHeaderValuesInChunk += streamValue
                    countOfNonNullMeasurements++
                }
            }
            val averageStreamValue = sumOfHeaderValuesInChunk / countOfNonNullMeasurements
            lineWithAveragedValuesParameters[currentStreamHeader.value] =
                averageStreamValue.toString()
        }
        return lineWithAveragedValuesParameters.joinToString(AB_DELIMITER)
    }
}
