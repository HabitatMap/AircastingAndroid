package pl.llp.aircasting.util.events

import pl.llp.aircasting.data.model.Session

class ExportSessionEvent(val session: Session, val email: String)
