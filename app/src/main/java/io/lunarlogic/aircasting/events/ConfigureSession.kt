package io.lunarlogic.aircasting.events

import io.lunarlogic.aircasting.models.Session

class ConfigureSession(val session: Session, val wifiSSID: String?, val wifiPassword: String?)
