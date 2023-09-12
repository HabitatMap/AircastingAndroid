package pl.llp.aircasting.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import pl.llp.aircasting.R

class BatteryLevelService: Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(BATTERY_LEVEL_NOTIFICATION_ID, updateNotification(0))
        return START_STICKY
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "battery level chanel"
            val descritionText = "channel for battery level notif"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(BATTERY_LEVEL_CHANNEL_ID, name, importance).apply { description = descritionText }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(batteryLevel: Int): Notification{
        return NotificationCompat.Builder(this, BATTERY_LEVEL_CHANNEL_ID)
            .setContentTitle("Battery Level")
            .setContentText("$batteryLevel%")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    companion object{
        const val BATTERY_LEVEL_CHANNEL_ID = "BatteryLevel"
        const val BATTERY_LEVEL_NOTIFICATION_ID = 101
    }
}