package pl.llp.aircasting.util.events

import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.LocalSession

class NoteEditedEvent(val note: Note?, val localSession: LocalSession?)
