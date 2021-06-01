package io.lunarlogic.aircasting.sensor

import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.events.AirBeamConnectionFailedEvent
import io.lunarlogic.aircasting.events.AirBeamConnectionSuccessfulEvent
import io.lunarlogic.aircasting.exceptions.BLENotSupported
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
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
            onConnectionFailed()
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
        onConnectionFailed()
    }

    override fun onDisconnect(deviceId: String) {
        // Not sure if we should try to reconnect from here or directly from AirbeamConnector
//        tryReconnect()
        stopSelf()
    }

    // ??
    private fun tryReconnect() {
        try {
            mAirBeamConnector?.reconnect()
        } catch (e: BLENotSupported) {
            errorHandler.handleAndDisplay(e)
            onConnectionFailed()
        }
    }

    private fun onConnectionFailed() {
        val event = AirBeamConnectionFailedEvent()
        EventBus.getDefault().post(event)
    }
}
