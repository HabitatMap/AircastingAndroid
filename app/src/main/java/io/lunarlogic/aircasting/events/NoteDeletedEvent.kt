package io.lunarlogic.aircasting.events

import io.lunarlogic.aircasting.models.Note
import io.lunarlogic.aircasting.models.Session

class NoteDeletedEvent(val note: Note?, val session: Session?)
