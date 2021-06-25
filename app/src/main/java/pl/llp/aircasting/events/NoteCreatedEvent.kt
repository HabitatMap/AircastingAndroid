package pl.llp.aircasting.events

import pl.llp.aircasting.models.Note
import pl.llp.aircasting.models.Session

class NoteCreatedEvent(val session: Session, val note: Note)
