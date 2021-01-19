package io.lunarlogic.aircasting.sensor

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.content.ContextCompat
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.events.AirBeamConnectionBleNotSupportedEvent
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

        fun startService(context: Context,deviceItem: DeviceItem) {
            val startIntent = Intent(context, AirbeamService::class.java)
            startIntent.putExtra(DEVICE_ITEM_KEY, deviceItem as Parcelable)
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
        val deviceItem = intent?.getParcelableExtra(DEVICE_ITEM_KEY) as DeviceItem
        val airBeamConnector = airbeamConnectorFactory.get(deviceItem)

        airBeamConnector?.registerListener(this)
        try {
            airBeamConnector?.connect(deviceItem)
        } catch (e: BLENotSupported) {
            errorHandler.handleAndDisplay(e)
            val event = AirBeamConnectionBleNotSupportedEvent()
            EventBus.getDefault().post(event)
        }
    }

    override fun onStopService() {
        // nothing
    }

    override fun notificationMessage(): String {
        return getString(R.string.ab_service_notification_message)
    }

    override fun onConnectionSuccessful(deviceItem: DeviceItem) {
        val event = AirBeamConnectionSuccessfulEvent(deviceItem)
        EventBus.getDefault().post(event)
    }

    override fun onConnectionFailed(deviceId: String) {
        // ignore
    }

    override fun onDisconnect(deviceId: String) {
        stopSelf()
    }
}
