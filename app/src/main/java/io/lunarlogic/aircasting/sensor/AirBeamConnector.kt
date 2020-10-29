package io.lunarlogic.aircasting.sensor

import io.lunarlogic.aircasting.events.ConfigureSession
import io.lunarlogic.aircasting.events.DisconnectExternalSensorsEvent
import io.lunarlogic.aircasting.events.SendSessionAuth
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class AirBeamConnector {
    interface Listener {
        fun onConnectionSuccessful(deviceId: String)
    }

    private var mListener: Listener? = null

    abstract fun connect(deviceItem: DeviceItem)
    abstract protected fun disconnect()
    abstract protected fun sendAuth(sessionUUID: String)
    abstract protected fun configureSession(session: Session, wifiSSID: String?, wifiPassword: String?)

    fun registerListener(listener: Listener) {
        mListener = listener
    }

    fun onConnectionSuccessful(deviceId: String) {
        mListener?.onConnectionSuccessful(deviceId)
    }

    @Subscribe
    fun onMessageEvent(event: SendSessionAuth) {
        sendAuth(event.sessionUUID)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(event: ConfigureSession) {
        configureSession(event.session, event.wifiSSID, event.wifiPassword)
    }

    @Subscribe
    fun onMessageEvent(event: DisconnectExternalSensorsEvent) {
        disconnect()
    }

    @Subscribe
    fun onMessageEvent(event: StopRecordingEvent) {
        disconnect()
    }

    protected fun registerToEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    protected fun unregisterFromEventBus() {
        EventBus.getDefault().unregister(this);
    }
}
