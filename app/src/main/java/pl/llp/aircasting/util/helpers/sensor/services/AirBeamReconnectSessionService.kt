package pl.llp.aircasting.util.helpers.sensor.services

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.di.modules.MainScope
import pl.llp.aircasting.di.modules.SyncActiveFlow
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.events.AirBeamDiscoveryFailedEvent
import javax.inject.Inject

class AirBeamReconnectSessionService: AirBeamRecordSessionService() {

    @Inject
    @SyncActiveFlow
    lateinit var syncActiveFlow: SharedFlow<Boolean>

    @Inject
    @MainScope
    lateinit var mainCoroutineScope: CoroutineScope

    companion object {
        fun startService(context: Context, deviceItem: DeviceItem?, sessionUUID: String? = null) {
            val startIntent = Intent(context, AirBeamReconnectSessionService::class.java)

            deviceItem?.let { deviceItem ->
                startIntent.putExtra(DEVICE_ITEM_KEY, deviceItem as Parcelable)
            }
            startIntent.putExtra(SESSION_UUID_KEY, sessionUUID)

            ContextCompat.startForegroundService(context, startIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        (application as AircastingApplication).userDependentComponent?.inject(this)

        mIntent = intent
        mDeviceItem = mIntent?.getParcelableExtra(DEVICE_ITEM_KEY) as? DeviceItem?
        observeSyncStatus()
        return super.onStartCommand(mIntent, flags, startId)
    }


    override fun onConnectionSuccessful(deviceItem: DeviceItem, sessionUUID: String?) {
        super.onConnectionSuccessful(deviceItem, sessionUUID)
        Log.v(TAG, "Reconnecting mobile session")
        mAirBeamConnector.reconnectMobileSession()
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamDiscoveryFailedEvent) {
        Log.d(TAG, "Stopping service after AirBeamDiscoveryFailedEvent")
        stopSelf()
    }

    override fun onDisconnect(deviceId: String) {
        Log.d(TAG, "Sensor disconnected: $deviceId")
    }

    private fun observeSyncStatus() = mainCoroutineScope.launch {
        syncActiveFlow.collect { isSyncActive ->
            if (isSyncActive) stopSelf()
        }
    }
}
