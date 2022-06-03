package pl.llp.aircasting.data.local.repository

import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.entity.ExtSessionsDBObject

class ExtSessionsLocalRepository {
    private val mDatabase = DatabaseProvider.get().extSession()

    fun insert(extSession: ExtSessionsDBObject) = mDatabase.insert(extSession)

    fun deleteFollowedSession(extSession: ExtSessionsDBObject) =
        mDatabase.delete(extSession)

    fun getAllFollowedExtSessions() = mDatabase.getAllFollowedSessions()
}