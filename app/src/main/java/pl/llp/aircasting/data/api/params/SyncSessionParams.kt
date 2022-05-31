package pl.llp.aircasting.data.api.params

import pl.llp.aircasting.data.model.Session

class SyncSessionParams {
    constructor(session: Session) {
        this.uuid = session.uuid
        this.deleted = session.deleted
        this.version = session.version
    }

    val uuid: String
    val deleted: Boolean
    val version: Int?
}
