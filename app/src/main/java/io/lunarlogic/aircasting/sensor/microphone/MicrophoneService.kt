package io.lunarlogic.aircasting.sensor.microphone

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.sensor.SensorService
import javax.inject.Inject

class MicrophoneService: SensorService() {
    @Inject
    lateinit var microphoneReader: MicrophoneReader

    companion object {
        fun startService(context: Context) {
            val startIntent = Intent(context, MicrophoneService::class.java)
            ContextCompat.startForegroundService(context, startIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as AircastingApplication
        val appComponent = app.appComponent
        appComponent.inject(this)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun startSensor(intent: Intent?) {
        microphoneReader.start()
    }

    override fun onStopService() {
        microphoneReader.stop()
    }

    override fun notificationMessage(): String {
        return getString(R.string.mic_service_notification_message)
    }
}
