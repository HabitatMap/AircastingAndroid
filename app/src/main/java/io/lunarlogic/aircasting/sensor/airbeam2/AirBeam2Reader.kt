package io.lunarlogic.aircasting.sensor.airbeam2

import com.google.common.io.CharStreams
import com.google.common.io.LineProcessor
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.sensor.ResponseParser
import org.greenrobot.eventbus.EventBus
import java.io.InputStream
import java.io.InputStreamReader

/**
 * This reader will be effectively use only in MOBILE sessions
 * Only when AirBeam is in the MOBILE mode the data is sent to the Android device
 * No NewMeasurementEvent will be posted for FIXED sessions
 */

class AirBeam2Reader(private val mErrorHandler: ErrorHandler) {
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
                println("MARYSIA:  responseParser.parse(line)")
                val newMeasurementEvent = responseParser.parse(line)
                println("MARYSIA:  responseParser event ${newMeasurementEvent}")
                println("MARYSIA:  responseParser event ${newMeasurementEvent?.measuredValue}")

                newMeasurementEvent?.let { EventBus.getDefault().post(newMeasurementEvent) }

                return true
            }

            override fun getResult(): Void? {
                return null
            }
        }
    }
}
