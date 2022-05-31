package pl.llp.aircasting.util.events

import pl.llp.aircasting.data.model.LocalSession

class ExportSessionEvent(val localSession: LocalSession, val email: String)
