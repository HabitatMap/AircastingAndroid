package pl.llp.aircasting.data.local.repository

import androidx.lifecycle.LiveData
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.entity.ExtSessionsDBObject

class ExtSessionsLocalRepository {
    private val mDatabase = DatabaseProvider.get().extSession()

    val getAllFollowedExtSessions: LiveData<List<ExtSessionsDBObject>> =
        mDatabase.getAllFollowedSessions()

    suspend fun insert(extSession: ExtSessionsDBObject) {
        mDatabase.insert(extSession)
    }

    suspend fun deleteFollowedSession(extSession: ExtSessionsDBObject) {
        mDatabase.delete(extSession)
    }
}