package pl.llp.aircasting.util.events

import pl.llp.aircasting.data.model.LocalSession

class StartRecordingEvent(val localSession: LocalSession, val wifiSSID: String?, val wifiPassword: String?)
