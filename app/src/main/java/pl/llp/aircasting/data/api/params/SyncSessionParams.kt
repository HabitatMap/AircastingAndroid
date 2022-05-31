package pl.llp.aircasting.data.api.params

import pl.llp.aircasting.data.model.LocalSession

class SyncSessionParams {
    constructor(localSession: LocalSession) {
        this.uuid = localSession.uuid
        this.deleted = localSession.deleted
        this.version = localSession.version
    }

    val uuid: String
    val deleted: Boolean
    val version: Int?
}
