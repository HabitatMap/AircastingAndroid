package io.lunarlogic.aircasting.sensor

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.content.ContextCompat
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.events.AirBeamConnectionBleNotSupportedEvent
import io.lunarlogic.aircasting.events.AirBeamConnectionSuccessfulEvent
import io.lunarlogic.aircasting.exceptions.BLENotSupported
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import org.greenrobot.eventbus.EventBus


class AirbeamService : SensorService(),
    AirBeamConnector.Listener {

    companion object {
        val DEVICE_ITEM_KEY = "inputExtraDeviceItem"

        fun startService(context: Context, message: String, deviceItem: DeviceItem) {
            println("MARYSIA: airbeam service startService")
            val startIntent = Intent(context, AirbeamService::class.java)
            startIntent.putExtra(MESSAGE_KEY, message)
            startIntent.putExtra(DEVICE_ITEM_KEY, deviceItem as Parcelable)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, AirbeamService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun startSensor(intent: Intent?) {
        println("MARYSIA: startService in ab service")
        val deviceItem = intent?.getParcelableExtra(DEVICE_ITEM_KEY) as DeviceItem
        val app = application as AircastingApplication
        val errorHandler = ErrorHandler(this)
        println("MARYSIA: device item  ${deviceItem.name}")
        val airBeamConnectorFactory = AirBeamConnectorFactory(this, Settings(app), errorHandler)
        println("MARYSIA:airBeamConnectorFactory  ${airBeamConnectorFactory}")
        val airBeamConnector = airBeamConnectorFactory.get(deviceItem)
        println("MARYSIA: connector ${airBeamConnector}")

        airBeamConnector?.registerListener(this)
        try {
            airBeamConnector?.connect(deviceItem)
        } catch (e: BLENotSupported) {
            errorHandler.handleAndDisplay(e)
            val event = AirBeamConnectionBleNotSupportedEvent()
            EventBus.getDefault().post(event)
        }
    }

    override fun onConnectionSuccessful(deviceItem: DeviceItem) {
        println("MARYSIA: onconnection successful")
        val event = AirBeamConnectionSuccessfulEvent(deviceItem)
        EventBus.getDefault().post(event)
    }

    override fun onConnectionFailed(deviceId: String) {
        // ignore
    }
}
