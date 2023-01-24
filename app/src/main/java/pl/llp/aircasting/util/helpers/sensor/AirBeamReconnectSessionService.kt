package pl.llp.aircasting.util.helpers.sensor

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import androidx.core.content.ContextCompat
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.events.AirBeamDiscoveryFailedEvent

class AirBeamReconnectSessionService: AirBeamRecordSessionService() {

    companion object {
        val SESSION_UUID_KEY = "inputExtraSessionUUID"
        val SESSION_DEVICE_ID_KEY = "inputExtraSessionDeviceId"

        fun startService(context: Context, sessionDeviceId: String?, deviceItem: DeviceItem?, sessionUUID: String? = null) {
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
        Log.v(TAG, "Reconnecting mobile session")
        mAirBeamConnector?.reconnectMobileSession()
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamDiscoveryFailedEvent) {
        stopSelf()
    }
}
