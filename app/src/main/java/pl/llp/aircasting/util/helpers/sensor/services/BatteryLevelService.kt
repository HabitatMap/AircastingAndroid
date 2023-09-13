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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.di.modules.MainScope
import pl.llp.aircasting.util.events.SensorDisconnectedEvent
import pl.llp.aircasting.util.events.StopRecordingEvent
import pl.llp.aircasting.util.extensions.safeRegister
import javax.inject.Inject


class BatteryLevelService : Service() {

    @Inject
    @MainScope
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var batteryLevelFlow: MutableSharedFlow<Int>

    private lateinit var notificationManager: NotificationManager

    private var batteryLastState: Int = 0
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d("BatteryService", "Service destroyed")
        unregisterFromEventBus()
        coroutineScope.cancel()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BatteryService", "Service Started")
        (application as AircastingApplication).userDependentComponent?.inject(this)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        startForeground(BATTERY_LEVEL_NOTIFICATION_ID, createNotification())
        observeBatteryFlow()
        registerToEventBus()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.battery_level_channel_name)
            val descriptionText = getString(R.string.battery_level_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(BATTERY_LEVEL_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String = ""): Notification {
        val builder = NotificationCompat.Builder(applicationContext, BATTERY_LEVEL_CHANNEL_ID)
            .setContentTitle(getString(R.string.battery_level_notification_title))
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.aircasting)
            .setOngoing(true)
        return builder.build()
    }

    private fun updateNotification(batteryLevel: Int) {
        if (isOutOfHysteresis(batteryLevel)) {
            Log.d("BatteryService", "Notification updated")

            notificationManager.notify(
                BATTERY_LEVEL_NOTIFICATION_ID,
                createNotification("$batteryLevel%")
            )
            batteryLastState = batteryLevel
        }
    }

    private fun isOutOfHysteresis(batteryLevel: Int): Boolean {
        val upperBoundary = batteryLastState + 2
        val lowerBoundary = batteryLastState - 3
        return batteryLevel !in lowerBoundary..upperBoundary
    }

    fun observeBatteryFlow() {
        coroutineScope.launch {
            batteryLevelFlow.collect {
                updateNotification(it)
            }
        }
    }

    @Subscribe
    fun onMessageEvent(event: StopRecordingEvent) {
        stopService(Intent(applicationContext, BatteryLevelService::class.java))
    }

    @Subscribe
    fun onMessageEvent(event: SensorDisconnectedEvent) {
        notificationManager.notify(
            BATTERY_LEVEL_NOTIFICATION_ID,
            createNotification(getString(R.string.battery_level_not_available))
        )
    }


    private fun registerToEventBus() {
        EventBus.getDefault().safeRegister(this)
    }

    private fun unregisterFromEventBus() {
        EventBus.getDefault().unregister(this)
    }

    companion object {
        private const val BATTERY_LEVEL_CHANNEL_ID = "BatteryLevel"
        private const val BATTERY_LEVEL_NOTIFICATION_ID = 101
    }
}
