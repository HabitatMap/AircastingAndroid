package io.lunarlogic.aircasting.sensor.microphone

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.sensor.SensorService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MicrophoneService : SensorService() {

    companion object {
        fun startService(context: Context, message: String) {
            val startIntent = Intent(context, MicrophoneService::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)
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
