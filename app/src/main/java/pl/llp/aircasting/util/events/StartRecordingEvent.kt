package pl.llp.aircasting.util.events

import pl.llp.aircasting.data.model.Session

class StartRecordingEvent(val session: Session, val wifiSSID: String?, val wifiPassword: String?)
