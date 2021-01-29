package io.lunarlogic.aircasting.sensor

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.content.ContextCompat
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.events.AirBeamConnectionFailedEvent
import io.lunarlogic.aircasting.events.AirBeamConnectionSuccessfulEvent
import io.lunarlogic.aircasting.exceptions.BLENotSupported
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


class AirbeamService: SensorService(),
    AirBeamConnector.Listener {

    @Inject
    lateinit var airbeamConnectorFactory: AirBeamConnectorFactory

    @Inject
    lateinit var errorHandler: ErrorHandler

    companion object {
        val DEVICE_ITEM_KEY = "inputExtraDeviceItem"
        val SESSION_UUID_KEY = "inputExtraSessionUUID"

        fun startService(context: Context, deviceItem: DeviceItem, sessionUUID: String? = null) {
            val startIntent = Intent(context, AirbeamService::class.java)

            startIntent.putExtra(DEVICE_ITEM_KEY, deviceItem as Parcelable)
            startIntent.putExtra(SESSION_UUID_KEY, sessionUUID)

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
        intent ?: return

        val deviceItem = intent.getParcelableExtra(DEVICE_ITEM_KEY) as DeviceItem
        val sessionUUID: String? = intent.getStringExtra(SESSION_UUID_KEY)
        
        val airBeamConnector = airbeamConnectorFactory.get(deviceItem)

        airBeamConnector?.registerListener(this)
        try {
            airBeamConnector?.connect(deviceItem, sessionUUID)
        } catch (e: BLENotSupported) {
            errorHandler.handleAndDisplay(e)
            onConnectionFailed()
        }
    }

    override fun onStopService() {
        // nothing
    }

    override fun notificationMessage(): String {
        return getString(R.string.ab_service_notification_message)
    }

    override fun onConnectionSuccessful(deviceItem: DeviceItem, sessionUUID: String?) {
        val event = AirBeamConnectionSuccessfulEvent(deviceItem, sessionUUID)
        EventBus.getDefault().post(event)
    }

    override fun onConnectionFailed(deviceId: String) {
        onConnectionFailed()
    }

    override fun onDisconnect(deviceId: String) {
        stopSelf()
    }

    private fun onConnectionFailed() {
        val event = AirBeamConnectionFailedEvent()
        EventBus.getDefault().post(event)
    }
}
