package pl.llp.aircasting.util.events

import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.Session

class NoteDeletedEvent(val note: Note?, val session: Session?)
