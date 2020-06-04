package io.lunarlogic.aircasting.networking.params

import io.lunarlogic.aircasting.sensor.Session

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