package io.lunarlogic.aircasting.sensor

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.content.ContextCompat
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem

open class AirBeamRecordSessionService: AirBeamService() {

    companion object {
        val DEVICE_ITEM_KEY = "inputExtraDeviceItem"
        val SESSION_UUID_KEY = "inputExtraSessionUUID"

        fun startService(context: Context, deviceItem: DeviceItem, sessionUUID: String? = null) {
            val startIntent = Intent(context, AirBeamRecordSessionService::class.java)

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

        connect(deviceItem, sessionUUID)
    }
}
