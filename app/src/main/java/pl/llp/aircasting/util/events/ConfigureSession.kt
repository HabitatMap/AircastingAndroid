package pl.llp.aircasting.util.events

import pl.llp.aircasting.data.api.services.FixedSessionConfig
import pl.llp.aircasting.data.model.Session

class ConfigureSession(
    val session: Session,
    val wifiSSID: String?,
    val wifiPassword: String?,
    val fixedSessionConfig: FixedSessionConfig? = null,
)
