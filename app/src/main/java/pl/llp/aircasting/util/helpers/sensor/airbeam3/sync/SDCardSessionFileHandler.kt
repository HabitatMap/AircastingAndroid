package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.apache.commons.lang3.time.DateUtils
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SDCardMeasurementsParsingError
import pl.llp.aircasting.util.extensions.addSeconds
import pl.llp.aircasting.util.extensions.calendar
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardCSVFileFactory.Companion.airBeamParams
import pl.llp.aircasting.util.helpers.services.AveragingService
import pl.llp.aircasting.util.helpers.services.AveragingWindow
import pl.llp.aircasting.util.helpers.services.MeasurementsAveragingHelper
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
    private val helper: MeasurementsAveragingHelper,
    private val averagingService: AveragingService,
    private val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val defaultScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : SDCardSessionFileHandler {

    private var currentChunkTime: Date? = null
    private var dbSession: SessionDBObject? = null
    private lateinit var finalAveragingWindow: AveragingWindow
    private var startTime: Long? = null
    private lateinit var csvSession: CSVSession

    override suspend fun handle(file: File): CSVSession? = try {
        val lines = file.readLines().filter {
            !SDCardCSVFileChecker.lineIsCorrupted(it)
        }
        val sessionUUID = CSVSession.uuidFrom(lines.firstOrNull())

        dbSession = sessionRepository.getSessionByUUIDSuspend(sessionUUID)
        if (dbSession == null) Log.v(TAG, "Could not find session with uuid: $sessionUUID in DB")
        startTime = dbSession?.startTime?.time
            ?: CSVSession.timestampFrom(lines.firstOrNull())?.time
        finalAveragingWindow = getFinalAveragingWindow(lines)
        val averagingFrequency = finalAveragingWindow.value
        Log.d(TAG, "${dbSession?.name} final averaging frequency: $averagingFrequency")
        csvSession = CSVSession(sessionUUID, averagingFrequency)

        val averageExistingMeasurementsJob = ioScope.launch {
            averagingService.stopAndPerformFinalAveraging(sessionUUID, finalAveragingWindow)
        }

        val averageFileMeasurementsJob = defaultScope.launch {
            currentChunkTime = try {
                DateUtils.truncate(dbSession?.startTime, Calendar.SECOND)
            } catch (e: Exception) {
                CSVSession.timestampFrom(lines.firstOrNull())
            }
            currentChunkTime = calendar().addSeconds(currentChunkTime, -1)
            incrementChunkTime(averagingFrequency)

            lines.chunked(averagingFrequency) { chunk ->
                // We do not include leftover measurements
                if (chunk.size < averagingFrequency) return@chunked

                averageMeasurementAndAddToSession(chunk)

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

    private fun getFinalAveragingWindow(
        lines: List<String>
    ): AveragingWindow {
        val firstMeasurementTime = startTime
        val lastMeasurementTime = CSVSession.timestampFrom(lines.lastOrNull())?.time
        Log.d(TAG, "First measurement time: $startTime")
        Log.d(TAG, "Last measurement time: $lastMeasurementTime")
        return if (firstMeasurementTime == null || lastMeasurementTime == null)
            AveragingWindow.ZERO
        else
            helper.calculateAveragingWindow(firstMeasurementTime, lastMeasurementTime)
    }

    private fun averageMeasurementAndAddToSession(chunk: List<String>) {
        val start = startTime ?: return
        val firstMeasurementTime = Date(start)
        CSVMeasurementStream.SUPPORTED_STREAMS.keys.forEach { currentStreamHeader ->
            val streamMeasurements: List<CSVMeasurement> = chunk.mapNotNull { line ->
                getCsvMeasurement(line, currentStreamHeader)
            }
            helper.averageMeasurements(
                streamMeasurements,
                firstMeasurementTime,
                finalAveragingWindow
            ) { averagedMeasurement, _ ->
                csvSession.addMeasurement(averagedMeasurement, currentStreamHeader)
            }
        }
    }

    private fun getCsvMeasurement(
        line: String,
        currentStreamHeader: SDCardCSVFileFactory.Header
    ): CSVMeasurement? {
        val params = line.airBeamParams()
        val value = getValueFor(params, currentStreamHeader)
            ?: return null
        val latitude = getValueFor(params, SDCardCSVFileFactory.Header.LATITUDE)
        val longitude = getValueFor(params, SDCardCSVFileFactory.Header.LONGITUDE)
        val dateString =
            "${params[SDCardCSVFileFactory.Header.DATE.value]} ${params[SDCardCSVFileFactory.Header.TIME.value]}"
        val time = DateConverter.fromString(
            dateString,
            dateFormat = CSVSession.DATE_FORMAT
        ) ?: return null

        return CSVMeasurement(
            value, latitude, longitude, time, finalAveragingWindow.value
        )
    }


    private fun getValueFor(line: List<String>, header: SDCardCSVFileFactory.Header): Double? {
        return try {
            line[header.value].toDouble()
        } catch (e: NumberFormatException) {
            null
        }
    }
}

//class LineMeasurement(
//    private val streamHeader: SDCardCSVFileFactory.Header,
//    private val line: String
//) : AverageableMeasurement {
//    private val lineParams get() = line.airBeamParams()
//    override var time: Date
//        get() = timestampFrom(line) ?: Date()
//        set(value) {
//            lineParams[SDCardCSVFileFactory.Header.DATE.value] = value
//        }
//    override var value: Double
//        get() = TODO("Not yet implemented")
//        set(value) {}
//    override var latitude: Double?
//        get() = TODO("Not yet implemented")
//        set(value) {}
//    override var longitude: Double?
//        get() = TODO("Not yet implemented")
//        set(value) {}
//
//}
