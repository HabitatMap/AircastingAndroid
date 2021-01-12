package io.lunarlogic.aircasting.sensor.microphone

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.sensor.SensorService

class MicrophoneService : SensorService() {

    companion object {
        fun startService(context: Context, message: String) {
            val startIntent = Intent(context, MicrophoneService::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            //TODO we must stop service when we finish session
            val stopIntent = Intent(context, MicrophoneService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun startSensor(intent: Intent?) {
        // TODO: can we use injector here?
        val audioReader = AudioReader()
        val errorHandler = ErrorHandler(this)
        val microphoneReader = MicrophoneReader(audioReader, errorHandler)
        microphoneReader.start()
    }
}
