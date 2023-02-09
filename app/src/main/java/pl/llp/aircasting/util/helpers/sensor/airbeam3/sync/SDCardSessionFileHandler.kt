package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.apache.commons.lang3.time.DateUtils
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SDCardMeasurementsParsingError
import pl.llp.aircasting.util.extensions.addSeconds
import pl.llp.aircasting.util.extensions.calendar
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardCSVFileFactory.Companion.AB_DELIMITER
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardCSVFileFactory.Companion.airBeamParams
import pl.llp.aircasting.util.helpers.services.AveragingService
import java.io.File
import java.io.IOException
import java.util.*

interface SDCardSessionFileHandler {
    suspend fun handle(file: File): CSVSession?
}

class SDCardSessionFileHandlerFixed(
    private val mErrorHandler: ErrorHandler
) : SDCardSessionFileHandler {
    override suspend fun handle(file: File): CSVSession? = try {
        val lines = file.readLines().filter {
            !SDCardCSVFileChecker.lineIsCorrupted(it)
        }
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
    private val measurementsRepository: MeasurementsRepositoryImpl,
    private val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val defaultScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : SDCardSessionFileHandler {

    private var currentChunkTime: Date? = null
    private var dbSession: SessionDBObject? = null

    override suspend fun handle(file: File): CSVSession? = try {
        val lines = file.readLines().filter {
            !SDCardCSVFileChecker.lineIsCorrupted(it)
        }
        val sessionUUID = CSVSession.uuidFrom(lines.firstOrNull())
        val csvSession = CSVSession(sessionUUID)

        dbSession = sessionRepository.getSessionByUUID(sessionUUID)
        if (dbSession == null) Log.v(TAG, "Could not find session with uuid: $sessionUUID in DB")
        val averagingFrequency = getFinalAveragingFrequency(lines)

        val averageExistingMeasurementsJob = ioScope.launch {
            AveragingService.get(dbSession?.id)
                ?.performFinalAveragingAfterSDSync(averagingFrequency)
        }

        val averageFileMeasurementsJob = defaultScope.launch {
            currentChunkTime = dbSession?.startTime ?: CSVSession.timestampFrom(lines.firstOrNull())
            lines.chunked(averagingFrequency) { chunk ->
                // We do not include leftover measurements
                if (chunk.size < averagingFrequency) return@chunked

                val averageMeasurement = averageMeasurementFrom(chunk)
                csvSession.addMeasurements(averageMeasurement, currentChunkTime)

                incrementChunkTime(averagingFrequency)
            }
        }

        joinAll(averageExistingMeasurementsJob, averageFileMeasurementsJob)

        csvSession
    } catch (e: IOException) {
        mErrorHandler.handle(SDCardMeasurementsParsingError(e))
        Log.v(TAG, e.stackTraceToString())
        null
    }

    private fun incrementChunkTime(averagingFrequency: Int) {
        currentChunkTime = calendar().addSeconds(currentChunkTime, averagingFrequency)
    }

    private fun getFinalAveragingFrequency(
        lines: List<String>
    ): Int {
        val firstMeasurementTime = try {
            DateUtils.truncate(dbSession?.startTime, Calendar.SECOND)
        } catch (e: Exception) {
            CSVSession.timestampFrom(lines.firstOrNull())
        }
        val lastMeasurementTime = CSVSession.timestampFrom(lines.lastOrNull())
        return AveragingService.getAveragingFrequency(firstMeasurementTime, lastMeasurementTime)
    }

    private fun averageMeasurementFrom(chunk: List<String>): String {
        /*
        * As the AB keeps all the measurements on SD card, even the ones that we have in local DB,
        * we could just average them all from the file values;
        * set time based on Session Start Time and the Averaging Threshold;
        * If there is a measurement in local DB with such time, we will grab it's location
        * to preserve the geolocation accuracy from the phone
        * (or use sql transaction to update all the measurement's data except for location,
        * instead of filtering them during processing?)
        * Questions:
        * 1. If session has already averaged measurements in local db, how to connect them to files ones?
        * It should just replace them with the averaged ones from file with their geo location
        * 2. Unify averaging strategy code to not have it here and also in AveragingService that works with DB measurements,
        * as the background averaging should still persist for incoming measurements when the AB is connected,
        * in case the user will not sync the SD
        * */
        val middleMeasurement = chunk[chunk.size / 2]
        val middleMeasurementParams = middleMeasurement.airBeamParams()
        val lineWithAveragedValuesParameters = middleMeasurementParams.toMutableList()
        setLocationFromDBIfExists(middleMeasurement, lineWithAveragedValuesParameters)

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

    private fun setLocationFromDBIfExists(
        middleMeasurement: String,
        lineWithAveragedValuesParameters: MutableList<String>
    ) {
        val date = CSVSession.timestampFrom(middleMeasurement)
        val location = measurementsRepository.getMeasurementsLocationAtTime(
            dbSession?.id,
            date
        )
        if (location != null) {
            lineWithAveragedValuesParameters[SDCardCSVFileFactory.Header.LATITUDE.value] =
                location.latitude.toString()
            lineWithAveragedValuesParameters[SDCardCSVFileFactory.Header.LONGITUDE.value] =
                location.longitude.toString()
        }
    }
}
