package pl.llp.aircasting.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.R
import pl.llp.aircasting.util.events.NewBatteryReadingEvent
import pl.llp.aircasting.util.extensions.safeRegister

class BatteryLevelService: Service() {
    private var battery: Int = 0
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d("BatteryService", "service destroyed")
        unregisterFromEventBus()
        super.onDestroy()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BatteryService", "service Started")
        createNotificationChannel()
        startForeground(BATTERY_LEVEL_NOTIFICATION_ID, updateNotification(0))
        registerToEventBus()
        return START_STICKY
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "battery level chanel"
            val descritionText = "channel for battery level notif"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(BATTERY_LEVEL_CHANNEL_ID, name, importance).apply { description = descritionText }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(batteryLevel: Int): Notification{
        Log.d("BatteryService", "notification updated")
        val builder = NotificationCompat.Builder(applicationContext, BATTERY_LEVEL_CHANNEL_ID)
            .setContentTitle("Battery Level")
            .setContentText("$batteryLevel%")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSmallIcon(R.drawable.ic_airbeam3)
            .setOngoing(true)
        return builder.build()
    }

    companion object{
        const val BATTERY_LEVEL_CHANNEL_ID = "BatteryLevel"
        const val BATTERY_LEVEL_NOTIFICATION_ID = 101
    }

    @Subscribe
    fun onMessageEvent(event: NewBatteryReadingEvent){
        Log.d("BatteryService", "event recived")
        update(event.percentage)
    }

    private fun update(batteryLevel: Int){
        if (batteryLevel != battery){
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(BATTERY_LEVEL_NOTIFICATION_ID, updateNotification(batteryLevel))
            battery = batteryLevel
        }

    }
    private fun registerToEventBus() {
        EventBus.getDefault().safeRegister(this)
    }

    private fun unregisterFromEventBus() {
        EventBus.getDefault().unregister(this)
    }
}