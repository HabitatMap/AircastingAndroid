package pl.llp.aircasting.events

import pl.llp.aircasting.models.Session

class UpdateSessionEvent(val session: Session, val name: String, val tags: ArrayList<String>)
