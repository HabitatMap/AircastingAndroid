package io.lunarlogic.aircasting.bluetooth

import android.bluetooth.BluetoothSocket
import com.google.common.io.CharStreams
import com.google.common.io.LineProcessor
import io.lunarlogic.aircasting.sensor.ResponseParser
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.exceptions.SensorResponseParsingError
import io.lunarlogic.aircasting.sensor.Measurement
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class BluetoothService() {
    val responseParser = ResponseParser()

    fun run(mmSocket: BluetoothSocket) {
        val mInputStream: InputStream = mmSocket.inputStream
        val inputStreamReader = InputStreamReader(mInputStream)

        CharStreams.readLines(
            inputStreamReader,
            lineProcessor()
        )
    }

    private fun lineProcessor(): LineProcessor<Void> {
        return object : LineProcessor<Void> {
            @Throws(IOException::class, SensorResponseParsingError::class)
            override fun processLine(line: String): Boolean {
                val newMeasurementEvent = responseParser.parse(line)
                EventBus.getDefault().post(newMeasurementEvent)

                return !Thread.interrupted()
            }

            override fun getResult(): Void? {
                return null
            }
        }
    }
}