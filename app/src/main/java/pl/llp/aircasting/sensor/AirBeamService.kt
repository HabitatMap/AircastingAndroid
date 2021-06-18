package pl.llp.aircasting.sensor

import pl.llp.aircasting.R
import pl.llp.aircasting.events.AirBeamConnectionFailedEvent
import pl.llp.aircasting.events.AirBeamConnectionSuccessfulEvent
import pl.llp.aircasting.exceptions.BLENotSupported
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


abstract class AirBeamService: SensorService(),
    AirBeamConnector.Listener {

    protected var mAirBeamConnector: AirBeamConnector? = null

    @Inject
    lateinit var airbeamConnectorFactory: AirBeamConnectorFactory

    @Inject
    lateinit var errorHandler: ErrorHandler

    protected fun connect(deviceItem: DeviceItem, sessionUUID: String? = null) {
        mAirBeamConnector = airbeamConnectorFactory.get(deviceItem)

        mAirBeamConnector?.registerListener(this)
        try {
            mAirBeamConnector?.connect(deviceItem, sessionUUID)
        } catch (e: BLENotSupported) {
            errorHandler.handleAndDisplay(e)
            onConnectionFailed(deviceItem)
        }
    }

    override fun onStopService() {
        mAirBeamConnector = null
    }

    override fun notificationMessage(): String {
        return getString(R.string.ab_service_notification_message)
    }

    override fun onConnectionSuccessful(deviceItem: DeviceItem, sessionUUID: String?) {
        val event = AirBeamConnectionSuccessfulEvent(deviceItem, sessionUUID)
        EventBus.getDefault().post(event)
    }

    override fun onConnectionFailed(deviceId: String) {
        onConnectionFailed(deviceId)
    }

    override fun onDisconnect(deviceId: String) {
        stopSelf()
    }

    private fun onConnectionFailed(deviceItem: DeviceItem) {
        val event = AirBeamConnectionFailedEvent(deviceItem)
        EventBus.getDefault().post(event)
    }
}
