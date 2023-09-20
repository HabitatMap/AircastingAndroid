package pl.llp.aircasting.util.helpers.sensor.airbeamNonSyncable.reader

import com.google.common.io.CharStreams
import com.google.common.io.LineProcessor
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.common.ResponseParser
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * This reader will be effectively use only in MOBILE sessions
 * Only when AirBeam is in the MOBILE mode the data is sent to the Android device
 * No NewMeasurementEvent will be posted for FIXED sessions
 */

class AirBeam2Reader @Inject constructor(private val mErrorHandler: ErrorHandler) {
    fun run(inputStream: InputStream) {
        val inputStreamReader = InputStreamReader(inputStream)

        CharStreams.readLines(
            inputStreamReader,
            lineProcessor()
        )
    }

    private fun lineProcessor(): LineProcessor<Void> {
        val responseParser = ResponseParser(mErrorHandler)

        return object : LineProcessor<Void> {
            override fun processLine(line: String): Boolean {
                val newMeasurementEvent = responseParser.parse(line)
                newMeasurementEvent?.let { EventBus.getDefault().post(newMeasurementEvent) }

                return true
            }

            override fun getResult(): Void? {
                return null
            }
        }
    }
}
