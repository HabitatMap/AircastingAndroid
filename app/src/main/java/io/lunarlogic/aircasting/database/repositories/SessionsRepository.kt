package io.lunarlogic.aircasting.database.repositories

import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.SessionDBObject
import io.lunarlogic.aircasting.models.Session
import java.util.*

class SessionsRepository {
    private val mDatabase = DatabaseProvider.get()

    fun insert(session: Session): Long {
        val sessionDBObject =
            SessionDBObject(session)
        return mDatabase.sessions().insert(sessionDBObject)
    }

    fun getMobileActiveSessionIdByDeviceId(deviceId: String): Long? {
        val sessionDBObject = mDatabase.sessions().loadSessionByDeviceIdStatusAndType(deviceId,
            Session.Status.RECORDING, Session.Type.MOBILE)

        if (sessionDBObject != null) {
            return sessionDBObject.id
        } else {
            return null
        }
    }

    fun getSessionByUUID(uuid: String): SessionDBObject? {
        return mDatabase.sessions().loadSessionByUUID(uuid)
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

    fun mobileSessionAlreadyExistsForDeviceId(deviceId: String): Boolean {
        return mDatabase.sessions()
            .loadSessionByDeviceIdStatusAndType(deviceId, Session.Status.RECORDING, Session.Type.MOBILE) != null
    }

    fun disconnectMobileSessions() {
        mDatabase.sessions().updateStatusAndEndTimeForSessionTypeAndExistingStatus(
            Session.Status.DISCONNECTED, Date(), Session.Type.MOBILE, Session.Status.RECORDING)
    }

    fun finishedSessions(): List<Session> {
        return mDatabase.sessions().byStatus(Session.Status.FINISHED)
            .map { dbObject -> Session(dbObject) }
    }

    fun fixedSessions(): List<SessionDBObject> {
        return mDatabase.sessions().byType(Session.Type.FIXED)
    }

    fun delete(uuids: List<String>) {
        if (!uuids.isEmpty()) {
            mDatabase.sessions().delete(uuids)
        }
    }

    fun markForRemoval(uuids: List<String>) {
        mDatabase.sessions().markForRemoval(uuids)
    }

    fun deleteMarkedForRemoval() {
        mDatabase.sessions().deleteMarkedForRemoval()
    }

    fun updateOrCreate(session: Session): Long? {
        val sessionDbObject = mDatabase.sessions().loadSessionByUUID(session.uuid)
        if (sessionDbObject == null) {
            return insert(session)
        } else {
            update(session)
            return null
        }
    }

    fun updateSessionStatus(session: Session, status: Session.Status) {
//        session.status = status
        mDatabase.sessions().updateStatus(session.uuid, status)
    }
}
