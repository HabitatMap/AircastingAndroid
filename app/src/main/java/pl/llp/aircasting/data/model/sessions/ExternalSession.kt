package pl.llp.aircasting.data.model.sessions

import pl.llp.aircasting.data.local.entity.ExtSessionsDBObject
import pl.llp.aircasting.data.model.LocalSession

open class ExternalSession(
    extSessionsDBObject: ExtSessionsDBObject
) : LocalSession(
    extSessionsDBObject
) {

}
