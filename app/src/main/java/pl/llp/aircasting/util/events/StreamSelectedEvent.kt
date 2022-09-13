package pl.llp.aircasting.util.events

import pl.llp.aircasting.data.model.Session

class StreamSelectedEvent(private val mSession: Session?) {
    val session get() = mSession
}