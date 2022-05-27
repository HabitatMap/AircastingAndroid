package pl.llp.aircasting.data.local.repository

import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.entity.ExtSessionsDBObject

class ExtSessionsLocalRepository {
    private val mDatabase = DatabaseProvider.get().extSession()

    suspend fun insert(extSession: ExtSessionsDBObject) {
        mDatabase.insert(extSession)
    }

    suspend fun deleteFollowedSession(extSession: ExtSessionsDBObject) {
        mDatabase.delete(extSession)
    }

    suspend fun getAllFollowedExtSessions() {
        mDatabase.getAllFollowedSessions()
    }
}