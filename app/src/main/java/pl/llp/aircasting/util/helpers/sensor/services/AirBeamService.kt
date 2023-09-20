package pl.llp.aircasting.util.helpers.sensor.services

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.modules.IoCoroutineScope
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.events.AirBeamConnectionFailedEvent
import pl.llp.aircasting.util.events.AirBeamConnectionSuccessfulEvent
import pl.llp.aircasting.util.events.SensorDisconnectedEvent
import pl.llp.aircasting.util.exceptions.BLENotSupported
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SensorDisconnectedError
import pl.llp.aircasting.util.helpers.sensor.common.connector.AirBeamConnector
import pl.llp.aircasting.util.helpers.sensor.common.connector.AirBeamConnectorFactory
import pl.llp.aircasting.util.helpers.sensor.common.connector.AirBeamReconnector
import javax.inject.Inject


abstract class AirBeamService : SensorService(),
    AirBeamConnector.Listener {

    protected lateinit var mAirBeamConnector: AirBeamConnector

    @Inject
    lateinit var airbeamConnectorFactory: AirBeamConnectorFactory

    @Inject
    lateinit var errorHandler: ErrorHandler

    @Inject
    lateinit var airbeamReconnector: AirBeamReconnector

    @Inject
    lateinit var mSessionRepository: SessionsRepository

    @Inject
    @IoCoroutineScope
    lateinit var coroutineScope: CoroutineScope

    protected fun connect(deviceItem: DeviceItem, sessionUUID: String? = null) {
        Log.d(TAG, "Creating AirBeamConnector")
        mAirBeamConnector = airbeamConnectorFactory.get(deviceItem)

        mAirBeamConnector.registerListener(this)

        try {
            mAirBeamConnector.connect(deviceItem, sessionUUID)
        } catch (e: BLENotSupported) {
            errorHandler.handleAndDisplay(e)
            onConnectionFailed(deviceItem)
        }
    }

    override fun onStopService() {
        Log.d(TAG, "Service stopping")
    }

    override fun notificationMessage(): String {
        return getString(R.string.ab_service_notification_message)
    }

    override fun onConnectionSuccessful(deviceItem: DeviceItem, sessionUUID: String?) {
        val event = AirBeamConnectionSuccessfulEvent(deviceItem, sessionUUID)
        EventBus.getDefault().post(event)
        errorHandler.handle(SensorDisconnectedError("called from AirBeamService, onConnectionSuccessful"))
    }

    override fun onDisconnect(deviceId: String) {
        Log.d(TAG, "Disconnecting and stopping service")
        stopSelf()
    }

    override fun onConnectionFailed(deviceItem: DeviceItem) {
        val event = AirBeamConnectionFailedEvent(deviceItem)
        EventBus.getDefault().post(event)
        errorHandler.handle(SensorDisconnectedError("called from AirBeamService, onConnectionFailed"))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: SensorDisconnectedEvent) {
        errorHandler.handle(SensorDisconnectedError("called from AirBeamService, number of reconnect tries ${airbeamReconnector.mReconnectionTriesNumber}"))

        event.sessionUUID?.let { sessionUUID ->
            coroutineScope.launch {
                val sessionDBObject = mSessionRepository.getSessionByUUID(sessionUUID)
                sessionDBObject?.let { sessionDBObject ->
                    val session = Session(sessionDBObject)
                    if (session.type == Session.Type.MOBILE && session.deviceId == event.sessionDeviceId) {
                        airbeamReconnector.tryToReconnectPeriodically(session, event.device)
                    }
                }
            }
        }
    }
}
