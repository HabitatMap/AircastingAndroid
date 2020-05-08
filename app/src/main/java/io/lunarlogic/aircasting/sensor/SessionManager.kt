package io.lunarlogic.aircasting.sensor

import io.lunarlogic.aircasting.events.NewMeasurementEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class SessionManager {
    private var mCurrentSession: Session? = null

    fun startRecording(session: Session) {
        EventBus.getDefault().register(this);
        mCurrentSession = session
    }

    @Subscribe
    fun onMessageEvent(event: NewMeasurementEvent) {
        // TODO: handle multiple sessions
        mCurrentSession?.addMeasurement(event.measurement)
    }
}