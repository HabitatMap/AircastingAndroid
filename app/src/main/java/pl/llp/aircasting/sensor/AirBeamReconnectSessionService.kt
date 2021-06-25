package pl.llp.aircasting.sensor

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.content.ContextCompat
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem

class AirBeamReconnectSessionService: AirBeamRecordSessionService() {
    companion object {
        val DEVICE_ITEM_KEY = "inputExtraDeviceItem"
        val SESSION_UUID_KEY = "inputExtraSessionUUID"

        fun startService(context: Context, deviceItem: DeviceItem, sessionUUID: String? = null) {
            val startIntent = Intent(context, AirBeamReconnectSessionService::class.java)

            startIntent.putExtra(DEVICE_ITEM_KEY, deviceItem as Parcelable)
            startIntent.putExtra(SESSION_UUID_KEY, sessionUUID)

            ContextCompat.startForegroundService(context, startIntent)
        }
    }

    override fun onConnectionSuccessful(deviceItem: DeviceItem, sessionUUID: String?) {
        super.onConnectionSuccessful(deviceItem, sessionUUID)
        mAirBeamConnector?.reconnectMobileSession()
    }
}
