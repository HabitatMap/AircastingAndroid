package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import com.opencsv.CSVReader
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SDCardMeasurementsParsingError
import java.io.File
import java.io.FileReader
import java.io.IOException

// needs to be divided into Mobile and Fixed
// Fixed can read everything at once and return the CSVSession
// Mobile will need to determine the averaging threshold based on Session.startTime and lastMeasurement.time from the file
interface ISDCardCSVIterator {
    fun read(file: File): CSVSession?
}
class SDCardCSVIteratorFixed(
    private val mErrorHandler: ErrorHandler
) : ISDCardCSVIterator {
    override fun read(file: File): CSVSession? = try {
        val reader = CSVReader(FileReader(file))
        var line: Array<String>? = reader.readNext()
        val sessionUUID = CSVSession.uuidFrom(line)
        val session = CSVSession(sessionUUID)

        while (line != null) {
            session.addMeasurements(line)
            line = reader.readNext()
        }

        session
    } catch (e: IOException) {
        mErrorHandler.handle(SDCardMeasurementsParsingError(e))

        null
    }
}

class SDCardCSVIteratorMobile(
    private val mErrorHandler: ErrorHandler
) : ISDCardCSVIterator {
    override fun read(file: File): CSVSession {
        TODO("Not yet implemented")
    }
}
