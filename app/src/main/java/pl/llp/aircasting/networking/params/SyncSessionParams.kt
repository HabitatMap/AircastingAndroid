package pl.llp.aircasting.networking.params

import pl.llp.aircasting.models.Session

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
