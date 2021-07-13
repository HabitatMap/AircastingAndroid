package pl.llp.aircasting.sensor

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.content.ContextCompat
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.events.AirBeamDiscoveryFailedEvent
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class AirBeamReconnectSessionService: AirBeamRecordSessionService() {
    private var mDiscoverySuccessful: Boolean? = null

    companion object {
        val DEVICE_ITEM_KEY = "inputExtraDeviceItem"
        val SESSION_UUID_KEY = "inputExtraSessionUUID"
        val SESSION_DEVICE_ID_KEY = "inputExtraSessionDeviceId"

        fun startService(context: Context, deviceId: String?, sessionUUID: String? = null) {
            println("MARYSIA: is reconnect service started?? maybe context is null? ${context}")
            val startIntent = Intent(context, AirBeamReconnectSessionService::class.java)

            startIntent.putExtra(SESSION_DEVICE_ID_KEY, deviceId)
            startIntent.putExtra(SESSION_UUID_KEY, sessionUUID)

            ContextCompat.startForegroundService(context, startIntent)
        }
    }

    override fun onConnectionSuccessful(deviceItem: DeviceItem, sessionUUID: String?) {
        super.onConnectionSuccessful(deviceItem, sessionUUID)
        mAirBeamConnector?.reconnectMobileSession()
    }

    override fun runAirBeamDiscoveryService(): Boolean? {
        val sessionDeviceId = mIntent?.getStringExtra(SESSION_DEVICE_ID_KEY)
        println("MARYSIA: device if from intent, runAirBeamDiscoveryService ${sessionDeviceId}")
        mAirBeamDiscoveryService.registerBluetoothDeviceFoundReceiver(this)
        mAirBeamDiscoveryService.find(
            deviceSelector = { deviceItem -> deviceItem.id == sessionDeviceId },
            onDiscoverySuccessful = { deviceItem -> onDiscoverySuccessful(deviceItem) },
            onDiscoveryFailed = { onDiscoveryFailed() },
            context = this
        )

        return mDiscoverySuccessful
    }

    fun onDiscoverySuccessful(deviceItem: DeviceItem) {
        println("MARYSIA: putting newly discovered device item into our Intent ${deviceItem}")
        mIntent?.putExtra(DEVICE_ITEM_KEY, deviceItem as Parcelable)
        mDiscoverySuccessful = true
    }

    fun onDiscoveryFailed() {
        mDiscoverySuccessful = false
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamDiscoveryFailedEvent) {
        println("MARYSIA: AirBeamDiscoveryFailedEvent in service, stopSelf()")
        stopSelf()
    }
}
