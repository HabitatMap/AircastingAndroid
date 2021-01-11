package io.lunarlogic.aircasting.sensor

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import java.io.Serializable


class AirbeamService : SensorService() {


    companion object {
        val DEVICE_ITEM_KEY = "inputExtraDeviceItem"

        fun startService(context: Context, message: String, deviceItem: DeviceItem) {
            println("MARYSIA: airbeam service startService")
            val startIntent = Intent(context, AirbeamService::class.java)
            startIntent.putExtra(MESSAGE_KEY, message)
            startIntent.putExtra(DEVICE_ITEM_KEY, deviceItem as Serializable)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, AirbeamService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun startSensor(intent: Intent?) {
        // TODO: take deviceItem from the intent extra, create AB connector and start recording
    }
}
