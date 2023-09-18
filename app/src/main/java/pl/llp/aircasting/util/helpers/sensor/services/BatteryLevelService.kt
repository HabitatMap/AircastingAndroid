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
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.di.modules.BatteryLevelFlow
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
    @BatteryLevelFlow
    lateinit var batteryLevelFlow: MutableSharedFlow<Int>

    private lateinit var notificationManager: NotificationManager
    private lateinit var job: Job

    private var batteryLastState: Int = 0
    private var lowBatteryNotificationSend = false
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d("BatteryService", "Service destroyed")
        unregisterFromEventBus()
        job.cancel()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BatteryService", "Service Started")
        (application as AircastingApplication).userDependentComponent?.inject(this)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels()
        startForeground(BATTERY_LEVEL_NOTIFICATION_ID, createNotification())
        observeBatteryFlow()
        registerToEventBus()
        return START_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val silentChannel = NotificationChannel(
                BATTERY_LEVEL_CHANNEL_ID,
                getString(R.string.battery_level_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.battery_level_channel_desc)
            }

            val alertChannel = NotificationChannel(
                BATTERY_LEVEL_ALERT_CHANNEL_ID,
                getString(R.string.battery_level_alert_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.battery_level_alert_channel_desc)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(silentChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    private fun getNotification(batteryLevel: Int): Notification {
        val notificationText = if (batteryLevel <= BATTERY_LOW_ALERT_LEVEL) {
            getString(R.string.battery_level_low, batteryLevel)
        } else {
            "$batteryLevel%"
        }

        return if (isLowBatteryNotificationToSend(batteryLevel)) {
            createNotification(notificationText, lowBatteryAlert = true)
        } else {
            createNotification(notificationText)
        }
    }

    private fun createNotification(
        contentText: String = "",
        lowBatteryAlert: Boolean = false
    ): Notification {

        val builder = if (lowBatteryAlert) {
            NotificationCompat.Builder(applicationContext, BATTERY_LEVEL_ALERT_CHANNEL_ID)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
        } else {
            NotificationCompat.Builder(applicationContext, BATTERY_LEVEL_CHANNEL_ID)
                .setDefaults(0)
                .setPriority(NotificationCompat.PRIORITY_LOW)
        }

        builder
            .setContentTitle(getString(R.string.battery_level_notification_title))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.aircasting)
            .setOngoing(true)

        return builder.build()
    }

    private fun updateNotification(batteryLevel: Int) {
        if (isOutOfHysteresis(batteryLevel)) {
            Log.d("BatteryService", "Notification updated")
            val notification = getNotification(batteryLevel)
            notificationManager.notify(
                BATTERY_LEVEL_NOTIFICATION_ID,
                notification
            )
            batteryLastState = batteryLevel
        }
    }

    private fun isOutOfHysteresis(batteryLevel: Int): Boolean {
        val upperBoundary = batteryLastState + 2
        val lowerBoundary = batteryLastState - 3
        return batteryLevel !in lowerBoundary..upperBoundary
    }

    private fun isLowBatteryNotificationToSend(percentage: Int): Boolean {
        return when {
            percentage <= BATTERY_LOW_ALERT_LEVEL && !lowBatteryNotificationSend -> {
                lowBatteryNotificationSend = true
                true
            }

            percentage > BATTERY_ALERT_RESET_LEVEL -> {
                lowBatteryNotificationSend = false
                false
            }

            else -> false
        }
    }


    private fun observeBatteryFlow() {
        job = coroutineScope.launch {
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
        private const val BATTERY_LEVEL_ALERT_CHANNEL_ID = "BatteryAlert"
        private const val BATTERY_LEVEL_NOTIFICATION_ID = 101
        private const val BATTERY_LOW_ALERT_LEVEL = 15
        private const val BATTERY_ALERT_RESET_LEVEL = 20
    }
}
