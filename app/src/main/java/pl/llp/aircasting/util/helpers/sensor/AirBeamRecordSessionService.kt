package pl.llp.aircasting.util.helpers.sensor

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.content.ContextCompat
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem

open class AirBeamRecordSessionService: AirBeamService() {
    var mIntent: Intent? = null
    var mDeviceItem: DeviceItem? = null

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
        (application as AircastingApplication).userDependentComponent?.inject(this)

        mIntent = intent
        mDeviceItem = mIntent?.getParcelableExtra(DEVICE_ITEM_KEY) as? DeviceItem?

        return super.onStartCommand(mIntent, flags, startId)
    }

    override fun startSensor(intent: Intent?) {
        intent ?: return

        // Device item loses data here
        mDeviceItem?.let { deviceItem ->
            val sessionUUID: String? = mIntent?.getStringExtra(SESSION_UUID_KEY)
            connect(deviceItem, sessionUUID)
        }
    }
}
