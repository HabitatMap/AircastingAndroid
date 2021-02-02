package io.lunarlogic.aircasting.sensor.airbeam3

import android.content.Context
import com.opencsv.CSVReaderHeaderAware
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.MeasurementsFromSDCardParsingError
import io.lunarlogic.aircasting.models.Session
import java.io.File
import java.io.FileReader
import java.io.IOException

class MeasurementsFromSDCardCreator(
    private val mContext: Context,
    private val mErrorHandler: ErrorHandler
) {
    private var mSession: Session? = null

    fun run() {
        // TODO: change naming and extract it somewhere
        val dir = mContext.getExternalFilesDir("sync")
        val file = File(dir, "sync.txt")
        val reader = CSVReaderHeaderAware(FileReader(file))

        try {
            var line: Map<String, String>
            while (reader.readMap().also { line = it } != null) {
                processLine(line)
            }
        } catch (e: IOException) {
            mErrorHandler.handle(MeasurementsFromSDCardParsingError(e))
        }
    }

    // TODO: extract processing to a separate class
    private fun processLine(line: Map<String, String>) {
        if (line.isEmpty()) return

        createMeasurement(line)
    }

    private fun createMeasurement(line: Map<String, String>) {
        if (mSession == null || mSession?.uuid != line[DownloadFromSDCardService.Header.UUID.value]) {
            // TODO: find a session
        }

        // TODO: create measurement
    }
}
