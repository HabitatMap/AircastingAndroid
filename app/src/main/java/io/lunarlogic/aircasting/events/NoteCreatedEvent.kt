package io.lunarlogic.aircasting.events

import io.lunarlogic.aircasting.models.Note
import io.lunarlogic.aircasting.models.Session

class NoteCreatedEvent(val session: Session, val note: Note)
