package pl.llp.aircasting.events

import pl.llp.aircasting.models.Session

class ExportSessionEvent(val session: Session, val email: String)
