package io.lunarlogic.aircasting.database.repositories

import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.SessionDBObject
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsAndMeasurementsDBObject
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
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

    fun getSessionWithMeasurementsByUUID(uuid: String): SessionWithStreamsAndMeasurementsDBObject? {
        return mDatabase.sessions().loadSessionAndMeasurementsByUUID(uuid)
    }

    fun getSessionIdByUUID(uuid: String): Long? {
        return mDatabase.sessions().loadSessionByUUID(uuid)?.id
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
        return mDatabase.sessions().loadSessionByDeviceIdStatusAndType(deviceId, Session.Status.RECORDING, Session.Type.MOBILE) != null ||
                mDatabase.sessions().loadSessionByDeviceIdStatusAndType(deviceId, Session.Status.DISCONNECTED, Session.Type.MOBILE) != null
    }

    fun disconnectMobileBluetoothSessions() {
        mDatabase.sessions().disconnectSessions(Session.Status.DISCONNECTED, DeviceItem.Type.MIC, Session.Type.MOBILE, Session.Status.RECORDING)
    }

    fun finishMobileMicSessions() {
        mDatabase.sessions().finishSessions(Session.Status.FINISHED, Date(), DeviceItem.Type.MIC, Session.Type.MOBILE, Session.Status.RECORDING)
    }

    fun disconnectSession(deviceId: String) {
        mDatabase.sessions().disconnectSession(Session.Status.DISCONNECTED, deviceId, Session.Status.RECORDING)
    }

    fun finishedSessions(): List<Session> {
        return mDatabase.sessions().byStatus(Session.Status.FINISHED)
            .map { dbObject -> Session(dbObject) }
    }

    fun fixedSessions(): List<SessionDBObject> {
        return mDatabase.sessions().byType(Session.Type.FIXED)
    }

    fun sessionsIdsByType(type: Session.Type): List<Long> {
        return mDatabase.sessions().loadSessionUuidsByType(type)
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
        mDatabase.sessions().updateStatus(session.uuid, status)
    }
}
