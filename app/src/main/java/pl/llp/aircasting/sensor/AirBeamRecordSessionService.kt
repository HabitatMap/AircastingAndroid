package pl.llp.aircasting.sensor

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.content.ContextCompat
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem
import javax.inject.Inject

open class AirBeamRecordSessionService: AirBeamService() {
    var mIntent: Intent? = null

    @Inject
    lateinit var mAirBeamDiscoveryService: AirBeamDiscoveryService

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

        mIntent = intent
        val discoverySuccessful = runAirBeamDiscoveryService()

        println("MARYSIA: We ran discovery, found an item, put to intent? ${mIntent?.extras}")
        return super.onStartCommand(mIntent, flags, startId)
    }

    override fun startSensor(intent: Intent?) {
        intent ?: return


        val deviceItem = mIntent?.getParcelableExtra(DEVICE_ITEM_KEY) as DeviceItem?
        println("MARYSIA: record session service startSensor device item id ${deviceItem?.id}")
        deviceItem?.let { deviceItem ->
            val sessionUUID: String? = mIntent?.getStringExtra(SESSION_UUID_KEY)
            println("MARYSIA: record session service startSensor trying to connect with ${deviceItem?.id}")
            connect(deviceItem, sessionUUID)
        }

    }

    open fun runAirBeamDiscoveryService(): Boolean? {
        // do nothing
        return null
    }
}
