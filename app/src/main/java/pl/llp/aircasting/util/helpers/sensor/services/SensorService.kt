package pl.llp.aircasting.util.helpers.sensor.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.StandaloneModeEvent
import pl.llp.aircasting.util.events.StopRecordingEvent
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.isSDKGreaterOrEqualToM
import pl.llp.aircasting.util.isSDKGreaterOrEqualToO
import pl.llp.aircasting.util.isSDKLessThanN
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * All of the devices need to connect (microphone/bluetooth) in the Foreground Service,
 * otherwise measurements will not record when the app is in background
 * https://trello.com/c/ysfz8lDq/1087-mobile-active-measurements-should-continue-recording-when-app-is-in-background
 */

abstract class SensorService : Service() {
    @Inject
    lateinit var settings: Settings

    private val CHANNEL_ID = "Aircasting ForegroundService"
    private val stoppedRecording = AtomicBoolean(false)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        registerToEventBus()

        startSensor(intent)
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = if (isSDKGreaterOrEqualToM())
            PendingIntent.getActivity(
                this,
                0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        else PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(notificationMessage())
            .setSmallIcon(R.drawable.aircasting)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        return START_REDELIVER_INTENT
    }

    override fun stopService(name: Intent?): Boolean {
        unregisterFromEventBus()
        onStopService()

        return super.stopService(name)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        setAppRestarted()
        unregisterFromEventBus()
        onStopService()
    }

    abstract fun startSensor(intent: Intent?)
    abstract fun onStopService()
    abstract fun notificationMessage(): String

    private fun createNotificationChannel() {
        if (isSDKGreaterOrEqualToO()) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    @Subscribe
    fun onMessageEvent(event: StopRecordingEvent) {
        stoppedRecording.set(true)
        stopSelf()
    }

    @Subscribe
    fun onMessageEvent(event: StandaloneModeEvent) {
        if (!isSDKLessThanN()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }

    private fun registerToEventBus() {
        EventBus.getDefault().safeRegister(this)
    }

    private fun unregisterFromEventBus() {
        EventBus.getDefault().unregister(this)
    }

    private fun setAppRestarted() {
        // After a crash or user killing the app we want to perform some action in MainActivity.onCreate()
        // so we are saving information about app restart in Settings
        // we need to do this because MainActivity can be destroyed when the app is in the background
        // https://stackoverflow.com/questions/59648644/foreground-service-content-intent-not-resuming-the-app-but-relaunching-it
        if (!stoppedRecording.get()) {
            settings.setAppRestarted()
        }
    }
}
