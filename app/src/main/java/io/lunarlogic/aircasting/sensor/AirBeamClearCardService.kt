package io.lunarlogic.aircasting.sensor

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.exceptions.BLENotSupported
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.sensor.airbeam3.sync.SDCardClearService
import io.lunarlogic.aircasting.sensor.airbeam3.sync.SyncEvent
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class AirBeamClearCardService: AirBeamService() {

    @Inject
    lateinit var airBeamDiscoveryService: AirBeamDiscoveryService

    @Inject
    lateinit var sdCardClearService: SDCardClearService

    companion object {
        fun startService(context: Context) {
            val startIntent = Intent(context, AirBeamClearCardService::class.java)
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

        airBeamDiscoveryService.find(
            deviceSelector = { deviceItem -> deviceItem.isSyncable() },
            onDiscoverySuccessful = { deviceItem -> connect(deviceItem) },
            onDiscoveryFailed = { onDiscoveryFailed() }
        )
    }

    override fun onConnectionSuccessful(deviceItem: DeviceItem, sessionUUID: String?) {
        showInfo("Connection to ${deviceItem.name} successful.")

        val airBeamConnector = mAirBeamConnector ?: return
        sdCardClearService.run(airBeamConnector)
    }

    private fun onDiscoveryFailed() {
        // TODO: remove it after implementing proper sync UI
        showInfo("Discovery failed.")
    }

    private fun showInfo(info: String) {
        EventBus.getDefault().post(SyncEvent(info))
    }
}
