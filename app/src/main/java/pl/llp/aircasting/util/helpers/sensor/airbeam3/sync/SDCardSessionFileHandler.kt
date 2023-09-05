package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import android.util.Log
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SDCardMeasurementsParsingError
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.CSVMeasurement
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.CSVSession
import pl.llp.aircasting.util.helpers.services.AveragingService
import pl.llp.aircasting.util.helpers.services.AveragingWindow
import pl.llp.aircasting.util.helpers.services.MeasurementsAveragingHelper
import java.io.File
import java.io.IOException
import java.util.Date

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
    // right CSVSession
) : SDCardSessionFileHandler {

    private var dbSession: SessionDBObject? = null
    private lateinit var finalAveragingWindow: AveragingWindow
    private var startTime: Long? = null
    private lateinit var csvSession: CSVSession

    override suspend fun handle(file: File): CSVSession? = try {
        val lines = file.readLines().filter {
            !SDCardCSVFileChecker.lineIsCorrupted(it)
        }
        val sessionUUID = CSVSession.uuidFrom(lines.firstOrNull())

        dbSession = sessionRepository.getSessionByUUID(sessionUUID)
        if (dbSession == null) Log.v(TAG, "Could not find session with uuid: $sessionUUID in DB")
        startTime = dbSession?.startTime?.time
            ?: CSVSession.timestampFrom(lines.firstOrNull())?.time
        finalAveragingWindow = getFinalAveragingWindow(lines)
        val averagingFrequency = finalAveragingWindow.value
        Log.d(TAG, "${dbSession?.name} final averaging frequency: $averagingFrequency")
        csvSession = CSVSession(sessionUUID, averagingFrequency)

        averagingService.stopAndPerformFinalAveraging(sessionUUID, finalAveragingWindow)

        lines.chunked(averagingFrequency).forEach { chunk ->
            // We do not include leftover measurements
            if (chunk.size < averagingFrequency) return@forEach

            averageMeasurementAndAddToSession(chunk)
        }

        csvSession
    } catch (e: IOException) {
        mErrorHandler.handle(SDCardMeasurementsParsingError(e))
        Log.v(TAG, e.stackTraceToString())
        null
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

    private suspend fun averageMeasurementAndAddToSession(chunk: List<String>) {
        val start = startTime ?: return
        val firstMeasurementTime = Date(start)
        CSVSession.SUPPORTED_STREAMS.keys.forEach { currentStreamHeader ->
            val streamMeasurements: List<CSVMeasurement> = chunk.mapNotNull { line ->
                getCsvMeasurement(line, currentStreamHeader)
            }
            helper.averageMeasurements(
                streamMeasurements,
                firstMeasurementTime,
                finalAveragingWindow
            ) { averagedMeasurement, _ ->
                Log.d(TAG, "SD Averaged measurement: $averagedMeasurement")
                csvSession.addMeasurement(averagedMeasurement, currentStreamHeader)
            }
        }
    }

    private fun getCsvMeasurement(
        line: String,
        currentStreamLineParameter: CSVSession.AB3LineParameter
    ): CSVMeasurement? {
        val params = CSVSession.lineParameters(line)
        val value = getValueFor(params, currentStreamLineParameter)
            ?: return null
        val latitude = getValueFor(params, CSVSession.AB3LineParameter.Latitude)
        val longitude = getValueFor(params, CSVSession.AB3LineParameter.Longitude)
        val dateString =
            "${params[CSVSession.AB3LineParameter.Date.position]} ${params[CSVSession.AB3LineParameter.Time.position]}"
        val time = DateConverter.fromString(
            dateString,
            dateFormat = CSVSession.DATE_FORMAT
        ) ?: return null

        return CSVMeasurement(value, latitude, longitude, time, finalAveragingWindow.value)
    }


    private fun getValueFor(line: List<String>, lineParameter: CSVSession.AB3LineParameter): Double? {
        return try {
            line[lineParameter.position].toDouble()
        } catch (e: NumberFormatException) {
            null
        }
    }
}
