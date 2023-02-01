package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SDCardMeasurementsParsingError
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardCSVFileFactory.Companion.AB_DELIMITER
import pl.llp.aircasting.util.helpers.services.AveragingService
import java.io.File
import java.io.FileReader
import java.io.IOException

// needs to be divided into Mobile and Fixed
// Fixed can read everything at once and return the CSVSession
// Mobile will need to determine the averaging threshold based on Session.startTime and lastMeasurement.time from the file
interface ISDCardCSVIterator {
    suspend fun read(file: File): CSVSession?
}

class SDCardCSVIteratorFixed(
    private val mErrorHandler: ErrorHandler
) : ISDCardCSVIterator {
    override suspend fun read(file: File): CSVSession? = try {
        val reader = CSVReader(withContext(Dispatchers.IO) {
            FileReader(file)
        })
        var line: Array<String>? = reader.readNext()
        val sessionUUID = CSVSession.uuidFrom(line)
        val csvSession = CSVSession(sessionUUID)

        while (line != null) {
            csvSession.addMeasurements(line)
            line = reader.readNext()
        }

        csvSession
    } catch (e: IOException) {
        mErrorHandler.handle(SDCardMeasurementsParsingError(e))

        null
    }
}

class SDCardCSVIteratorMobile(
    private val mErrorHandler: ErrorHandler,
    private val sessionRepository: SessionsRepository
) : ISDCardCSVIterator {
    override suspend fun read(file: File): CSVSession? = try {
        /*
        * 1. open file
        * 2. check averaging threshold
        * 3. average on the go
        * */
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

            val middleLine = chunk[chunk.size / 2]
//                TODO()
            csvSession.addMeasurements(middleLine.split(AB_DELIMITER).toTypedArray())
        }

        csvSession
    } catch (e: IOException) {
        mErrorHandler.handle(SDCardMeasurementsParsingError(e))

        null
    }
}
