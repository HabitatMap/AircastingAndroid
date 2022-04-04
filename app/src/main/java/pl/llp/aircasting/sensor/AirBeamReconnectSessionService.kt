package pl.llp.aircasting.sensor

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.content.ContextCompat
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.events.AirBeamDiscoveryFailedEvent
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem

class AirBeamReconnectSessionService: AirBeamRecordSessionService() {

    companion object {
        private const val SESSION_UUID_KEY = "inputExtraSessionUUID"
        private const val SESSION_DEVICE_ID_KEY = "inputExtraSessionDeviceId"

        fun startService(
            context: Context,
            sessionDeviceId: String?,
            deviceItem: DeviceItem?,
            sessionUUID: String? = null
        ) {
            val startIntent = Intent(context, AirBeamReconnectSessionService::class.java)

            startIntent.putExtra(SESSION_DEVICE_ID_KEY, sessionDeviceId)
            deviceItem?.let { deviceItem ->
                startIntent.putExtra(AirBeamSyncService.DEVICE_ITEM_KEY, deviceItem as Parcelable)
            }
            startIntent.putExtra(SESSION_UUID_KEY, sessionUUID)

            ContextCompat.startForegroundService(context, startIntent)
        }
    }

    override fun onConnectionSuccessful(deviceItem: DeviceItem, sessionUUID: String?) {
        super.onConnectionSuccessful(deviceItem, sessionUUID)
        mAirBeamConnector?.reconnectMobileSession()
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamDiscoveryFailedEvent) {
        stopSelf()
    }
}
