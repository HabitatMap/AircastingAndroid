package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import pl.llp.aircasting.util.exceptions.ErrorHandler
import java.io.File

// needs to be divided into Mobile and Fixed
// Fixed can read everything at once and return the CSVSession
// Mobile will need to determine the averaging threshold based on Session.startTime and lastMeasurement.time from the file
interface ISDCardCSVIterator {
    fun run(file: File): CSVSession
}
class SDCardCSVIteratorFixed(
    private val mErrorHandler: ErrorHandler
) : ISDCardCSVIterator {
    override fun run(file: File): CSVSession {
//        try {
//            val reader = CSVReader(FileReader(file))
//            var previousSessionUUID: String? = null
//            var currentSession: CSVSession? = null
//
//            while (true) {
//                val line: Array<String>? = reader.readNext()
//
//                if (line == null) {
//                    if (currentSession != null) {
//                        yield(currentSession)
//                    }
//                    break
//                }
//
//                val currentSessionUUID = CSVSession.uuidFrom(line)
//
//                if (currentSessionUUID != previousSessionUUID) {
//                    if (currentSession != null) {
//                        yield(currentSession)
//                    }
//
//                    currentSession = CSVSession(currentSessionUUID!!)
//                    previousSessionUUID = currentSessionUUID
//                }
//
//                currentSession?.addMeasurements(line)
//            }
//        } catch (e: IOException) {
//            mErrorHandler.handle(SDCardMeasurementsParsingError(e))
//        }
        TODO("Not yet implemented")
    }
}

class SDCardCSVIteratorMobile(
    private val mErrorHandler: ErrorHandler
) : ISDCardCSVIterator {
    override fun run(file: File): CSVSession {
        TODO("Not yet implemented")
    }
}
