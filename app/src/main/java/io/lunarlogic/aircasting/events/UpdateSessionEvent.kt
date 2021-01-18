package io.lunarlogic.aircasting.events

import io.lunarlogic.aircasting.models.Session

class UpdateSessionEvent(val session: Session, val name: String, val tags: ArrayList<String>)
