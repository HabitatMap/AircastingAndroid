package io.lunarlogic.aircasting.sensor.airbeam2

import android.bluetooth.BluetoothSocket
import com.google.common.io.CharStreams
import com.google.common.io.LineProcessor
import io.lunarlogic.aircasting.sensor.ResponseParser
import io.lunarlogic.aircasting.exceptions.SensorResponseParsingError
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.io.InputStreamReader

class AirBeam2Reader() {
    fun run(mmSocket: BluetoothSocket) {
        val inputStream = mmSocket.inputStream
        val inputStreamReader = InputStreamReader(inputStream)

        CharStreams.readLines(
            inputStreamReader,
            lineProcessor()
        )
    }

    private fun lineProcessor(): LineProcessor<Void> {
        val responseParser = ResponseParser()

        return object : LineProcessor<Void> {
            @Throws(IOException::class, SensorResponseParsingError::class)
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