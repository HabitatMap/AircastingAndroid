package io.lunarlogic.aircasting.events

import io.lunarlogic.aircasting.models.Session

class ExportSessionEvent(val session: Session, val email: String)
