package io.lunarlogic.aircasting.sensor.airbeam2

import com.google.common.io.CharStreams
import com.google.common.io.LineProcessor
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.sensor.ResponseParser
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

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
