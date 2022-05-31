package pl.llp.aircasting.util.events

import pl.llp.aircasting.data.model.LocalSession

class UpdateSessionEvent(val localSession: LocalSession, val name: String, val tags: ArrayList<String>)
