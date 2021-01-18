package io.lunarlogic.aircasting.sensor.microphone

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.sensor.SensorService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MicrophoneService : SensorService() {
    var mMicrophoneReader: MicrophoneReader? = null

    companion object {
        fun startService(context: Context) {
            val startIntent = Intent(context, MicrophoneService::class.java)
            ContextCompat.startForegroundService(context, startIntent)
        }
    }

    override fun startSensor(intent: Intent?) {
        // TODO: can we use injector here not to create this var from scratch?
        val app = application as AircastingApplication
        mMicrophoneReader= MicrophoneReader(AudioReader(), ErrorHandler(this), Settings(app))

        mMicrophoneReader?.start()
    }

    override fun onStopService() {
        mMicrophoneReader?.stop()
    }

    override fun notificationMessage(): String {
        return getString(R.string.mic_service_notification_message)
    }
}
