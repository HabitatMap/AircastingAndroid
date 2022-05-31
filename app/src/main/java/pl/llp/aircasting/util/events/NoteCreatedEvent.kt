package pl.llp.aircasting.util.events

import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.LocalSession

class NoteCreatedEvent(val localSession: LocalSession, val note: Note)
