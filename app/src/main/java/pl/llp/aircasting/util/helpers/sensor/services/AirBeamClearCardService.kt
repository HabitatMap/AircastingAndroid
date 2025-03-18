package pl.llp.aircasting.util.helpers.sensor.services

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.content.ContextCompat
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.exceptions.AirbeamServiceError

class AirBeamClearCardService : AirBeamService() {

    companion object {
        val DEVICE_ITEM_KEY = "inputExtraDeviceItem"

        fun startService(context: Context, deviceItem: DeviceItem) {
            val startIntent = Intent(context, AirBeamClearCardService::class.java)

            startIntent.putExtra(DEVICE_ITEM_KEY, deviceItem as Parcelable)

            ContextCompat.startForegroundService(context, startIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        (application as AircastingApplication).userDependentComponent?.inject(this)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun startSensor(intent: Intent?) {
        intent ?: return

        val deviceItem =
            intent.getParcelableExtra<DeviceItem>(AirBeamRecordSessionService.DEVICE_ITEM_KEY)

        if (deviceItem == null) {
            errorHandler.handle(AirbeamServiceError("DeviceItem passed through intent is null"))
        } else {
            connect(deviceItem)
        }
    }

    override fun onConnectionSuccessful(deviceItem: DeviceItem, sessionUUID: String?) {
        mAirBeamConnector.apply {
            clearSDCard()
        }
    }
}
