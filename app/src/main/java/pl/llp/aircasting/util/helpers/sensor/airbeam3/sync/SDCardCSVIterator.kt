package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import com.opencsv.CSVReader
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SDCardMeasurementsParsingError
import java.io.File
import java.io.FileReader
import java.io.IOException

class SDCardCSVIterator(
    private val mErrorHandler: ErrorHandler
) {
    fun run(file: File) = sequence {
        try {
            val reader = CSVReader(FileReader(file))
            var previousSessionUUID: String? = null
            var currentSession: CSVSession? = null

            while (true) {
                val line: Array<String>? = reader.readNext()

                if (line == null) {
                    if (currentSession != null) {
                        yield(currentSession)
                    }
                    break
                }

                val currentSessionUUID = CSVSession.uuidFrom(line)

                if (currentSessionUUID != previousSessionUUID) {
                    if (currentSession != null) {
                        yield(currentSession)
                    }

                    currentSession = CSVSession(currentSessionUUID!!)
                    previousSessionUUID = currentSessionUUID
                }

                currentSession?.addMeasurements(line)
            }
        } catch (e: IOException) {
            mErrorHandler.handle(SDCardMeasurementsParsingError(e))
        }
    }
}
