package io.lunarlogic.aircasting.database.repositories

import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.SessionDBObject
import io.lunarlogic.aircasting.sensor.Session

class SessionsRepository {
    private val mDatabase = DatabaseProvider.get()

    fun insert(session: Session): Long {
        val sessionDBObject =
            SessionDBObject(session)
        return mDatabase.sessions().insert(sessionDBObject)
    }

    fun getActiveSessionIdByDeviceId(deviceId: String): Long? {
        val sessionDBObject = mDatabase.sessions().loadSessionByByDeviceIdAndStatus(deviceId, Session.Status.RECORDING)

        if (sessionDBObject != null) {
            return sessionDBObject.id
        } else {
            return null
        }
    }

    fun loadSessionAndMeasurementsByUUID(uuid: String): Session? {
        val sessionDBObject = mDatabase.sessions().loadSessionAndMeasurementsByUUID(uuid)

        if (sessionDBObject != null) {
            return Session(sessionDBObject)
        } else {
            return null
        }
    }

    fun update(session: Session) {
        mDatabase.sessions().update(session.uuid, session.name, session.tags,
            session.endTime!!, session.status)
    }

    fun alreadyExistsForDeviceId(deviceId: String): Boolean {
        return mDatabase.sessions().loadSessionByByDeviceIdAndStatus(deviceId, Session.Status.RECORDING) != null
    }
}