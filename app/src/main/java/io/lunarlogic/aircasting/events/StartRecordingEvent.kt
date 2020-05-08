package io.lunarlogic.aircasting.events

import io.lunarlogic.aircasting.sensor.Session

class StartRecordingEvent(private val mSession: Session) {
    val session: Session get() = mSession
}