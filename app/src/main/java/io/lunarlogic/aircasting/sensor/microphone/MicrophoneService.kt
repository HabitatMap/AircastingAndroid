package io.lunarlogic.aircasting.sensor.microphone

import android.R
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.screens.main.MainActivity


class MicrophoneService : Service() {
    private val CHANNEL_ID = "ForegroundService Kotlin"


    companion object {
        fun startService(context: Context, message: String) {
            println("MARYSIA: microphpone service startService")
            val startIntent = Intent(context, MicrophoneService::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, MicrophoneService::class.java)
            context.stopService(stopIntent)
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        super.onStartCommand(intent, flags, startId)
println("MARYSIA: microphpone start? onStartCommand")
        val audioReader = AudioReader()
        val errorHandler = ErrorHandler(this)
        val microphoneReader = MicrophoneReader(audioReader, errorHandler)
        microphoneReader.start()
        val input = intent?.getStringExtra("inputExtra")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Aircasting: Microphone Service")
            .setContentText(input)
            .setSmallIcon(R.drawable.aircasting)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }
}
