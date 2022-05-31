package pl.llp.aircasting.util.events

import pl.llp.aircasting.data.model.Session

class UpdateSessionEvent(val session: Session, val name: String, val tags: ArrayList<String>)
