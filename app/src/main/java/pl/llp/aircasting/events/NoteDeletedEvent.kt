package pl.llp.aircasting.events

import pl.llp.aircasting.models.Note
import pl.llp.aircasting.models.Session

class NoteDeletedEvent(val note: Note?, val session: Session?)
