package io.lunarlogic.aircasting.events

import io.lunarlogic.aircasting.sensor.Session

class StartRecordingEvent(val session: Session, val wifiSSID: String?, val wifiPassword: String?)