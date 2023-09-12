package pl.llp.aircasting.util.helpers.sensor.services

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
import pl.llp.aircasting.util.events.StopRecordingEvent
import pl.llp.aircasting.util.extensions.safeRegister

class BatteryLevelService: Service() {
    private var batteryLastState: Int = 0
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d("BatteryService", "Service destroyed")
        unregisterFromEventBus()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BatteryService", "Service Started")
        createNotificationChannel()
        startForeground(BATTERY_LEVEL_NOTIFICATION_ID, createNotification())
        registerToEventBus()
        return START_STICKY
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "batteryLastState level chanel" //TODO(12.09.2023, Seb): replace with String resource
            val descritionText = "channel for batteryLastState level notif"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(BATTERY_LEVEL_CHANNEL_ID, name, importance).apply { description = descritionText }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String = ""): Notification{
        val builder = NotificationCompat.Builder(applicationContext, BATTERY_LEVEL_CHANNEL_ID)
            .setContentTitle("Battery Level") //TODO: stringResource
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.aircasting)
            .setOngoing(true)
        return builder.build()
    }

    private fun updateNotification(batteryLevel: Int){
        if (batteryLevel != batteryLastState){
            Log.d("BatteryService", "Notification updated")
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(BATTERY_LEVEL_NOTIFICATION_ID, createNotification("$batteryLevel%"))
            batteryLastState = batteryLevel
        }

    }

    @Subscribe
    fun onMessageEvent(event: NewBatteryReadingEvent){
        Log.d("BatteryService", "event recived")
        updateNotification(event.percentage)
    }

    @Subscribe
    fun onMessageEvent(event: StopRecordingEvent){
        stopService(Intent(applicationContext, BatteryLevelService::class.java))
    }


    private fun registerToEventBus() {
        EventBus.getDefault().safeRegister(this)
    }

    private fun unregisterFromEventBus() {
        EventBus.getDefault().unregister(this)
    }

    companion object{
        private const val BATTERY_LEVEL_CHANNEL_ID = "BatteryLevel"
        private const val BATTERY_LEVEL_NOTIFICATION_ID = 101
    }
}
