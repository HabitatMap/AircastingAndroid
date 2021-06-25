package pl.llp.aircasting.events

import pl.llp.aircasting.models.Session

class ConfigureSession(val session: Session, val wifiSSID: String?, val wifiPassword: String?)
