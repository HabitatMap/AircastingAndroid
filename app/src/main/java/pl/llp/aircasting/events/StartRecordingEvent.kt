package pl.llp.aircasting.events

import pl.llp.aircasting.models.Session

class StartRecordingEvent(val session: Session, val wifiSSID: String?, val wifiPassword: String?)
