package pl.llp.aircasting.events

import pl.llp.aircasting.models.Note
import pl.llp.aircasting.models.Session

class NoteEditedEvent(val note: Note?, val session: Session?)
