package pl.llp.aircasting.data.local.repository

import android.util.Log
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import java.util.*

class SessionsRepository {
    private val mDatabase = DatabaseProvider.get()

    fun insert(session: Session): Long {
        val sessionDBObject =
            SessionDBObject(session)
        return mDatabase.sessions().insert(sessionDBObject)
    }

    suspend fun insertSuspend(session: Session): Long {
        val sessionDBObject =
            SessionDBObject(session)
        return mDatabase.sessions().insertSuspend(sessionDBObject)
    }

    suspend fun getMobileActiveSessionIdByDeviceId(deviceId: String): Long? {
        val sessionDBObject = mDatabase.sessions().loadSessionByDeviceIdStatusAndTypeSuspend(
            deviceId,
            Session.Status.RECORDING, Session.Type.MOBILE
        )

        return sessionDBObject?.id
    }

    fun getSessionByUUID(uuid: String?): SessionDBObject? {
        uuid ?: return null

        return mDatabase.sessions().loadSessionByUUID(uuid)
    }

    suspend fun getSessionByUUIDSuspend(uuid: String?): SessionDBObject? {
        uuid ?: return null

        return mDatabase.sessions().loadSessionByUUIDSuspend(uuid)
    }

    suspend fun getSessionByIdSuspend(id: Long): SessionDBObject? {
        return mDatabase.sessions().loadSessionByIdSuspend(id)
    }

    fun getSessionById(id: Long): SessionDBObject? {
        return mDatabase.sessions().loadSessionById(id)
    }

    suspend fun getSessionWithMeasurementsByUUID(uuid: String): SessionWithStreamsAndMeasurementsDBObject? {
        return mDatabase.sessions().loadSessionAndMeasurementsByUUIDSuspend(uuid)
    }

    fun getSessionIdByUUID(uuid: String): Long? {
        return mDatabase.sessions().loadSessionByUUID(uuid)?.id
    }

    suspend fun getSessionIdByUUIDSuspend(uuid: String): Long? {
        return mDatabase.sessions().loadSessionByUUIDSuspend(uuid)?.id
    }

    fun loadSessionAndMeasurementsByUUID(uuid: String): Session? {
        val sessionDBObject = mDatabase.sessions().loadSessionAndMeasurementsByUUID(uuid)

        sessionDBObject ?: return null
        return Session(sessionDBObject)
    }

    fun loadSessionForUpload(uuid: String): Session? {
        val sessionForUploadDBObject = mDatabase.sessions().loadSessionForUploadByUUID(uuid)

        return sessionForUploadDBObject?.let { Session(it) }
    }

    suspend fun loadSessionForUploadSuspend(uuid: String): Session? {
        val sessionForUploadDBObject = mDatabase.sessions().loadSessionForUploadByUUIDSuspend(uuid)

        return sessionForUploadDBObject?.let { Session(it) }
    }

    fun update(session: Session) {
        session.endTime?.let {
            mDatabase.sessions().update(
                session.uuid, session.name, session.tags,
                it, session.status, session.version, session.urlLocation
            )
        }
    }

    suspend fun updateSuspend(session: Session) {
        session.endTime?.let {
            mDatabase.sessions().updateSuspend(
                session.uuid, session.name, session.tags,
                it, session.status, session.version, session.urlLocation
            )
        }
    }

    fun mobileSessionAlreadyExistsForDeviceId(deviceId: String): Boolean {
        return mDatabase.sessions().loadSessionByDeviceIdStatusAndType(
            deviceId,
            Session.Status.RECORDING,
            Session.Type.MOBILE
        ) != null ||
                mDatabase.sessions().loadSessionByDeviceIdStatusAndType(
                    deviceId,
                    Session.Status.DISCONNECTED,
                    Session.Type.MOBILE
                ) != null
    }

    fun isMicrophoneSessionAlreadyRecording(): Boolean {
        return mDatabase.sessions().loadSessionByStatusTypeAndDeviceType(
            Session.Status.RECORDING,
            Session.Type.MOBILE,
            DeviceItem.Type.MIC
        ) != null
    }

    fun mobileActiveSessionExists(): Boolean {
        return mDatabase.sessions()
            .loadSessionByStatusAndType(Session.Status.RECORDING, Session.Type.MOBILE) != null ||
                mDatabase.sessions().loadSessionByStatusAndType(
                    Session.Status.DISCONNECTED,
                    Session.Type.MOBILE
                ) != null
    }

    fun disconnectMobileBluetoothSessions() {
        mDatabase.sessions().disconnectSessions(
            Session.Status.DISCONNECTED,
            DeviceItem.Type.MIC,
            Session.Type.MOBILE,
            Session.Status.RECORDING
        )
    }

    fun finishMobileMicSessions() {
        mDatabase.sessions().finishSessions(
            Session.Status.FINISHED,
            Date(),
            DeviceItem.Type.MIC,
            Session.Type.MOBILE,
            Session.Status.RECORDING
        )
    }

    suspend fun disconnectSession(deviceId: String) {
        mDatabase.sessions()
            .disconnectSession(Session.Status.DISCONNECTED, deviceId, Session.Status.RECORDING)
    }

    suspend fun allSessionsExceptRecording(): List<Session> {
        return mDatabase.sessions().byStatus(Session.Status.FINISHED)
            .map { dbObject -> Session(dbObject) }
    }

    fun fixedSessions(): List<SessionDBObject> {
        return mDatabase.sessions().byType(Session.Type.FIXED)
    }

    suspend fun sessionsIdsByType(type: Session.Type): List<Long> {
        return mDatabase.sessions().loadSessionUuidsByType(type)
    }

    suspend fun delete(uuids: List<String>) {
        if (uuids.isNotEmpty()) {
            mDatabase.sessions().delete(uuids)
        }
    }

    fun delete(uuid: String) {
        mDatabase.sessions().delete(uuid)
    }

    suspend fun markForRemoval(uuid: String) {
        Log.d(TAG, "Marking $uuid for deletion")
        mDatabase.sessions().markForRemoval(uuid)
    }

    fun updateOrCreate(session: Session): Long? {
        val sessionDbObject = mDatabase.sessions().loadSessionByUUID(session.uuid)
        return if (sessionDbObject == null) {
            insert(session)
        } else {
            update(session)
            null
        }
    }

    fun updateSessionStatus(session: Session, status: Session.Status) {
        mDatabase.sessions().updateStatus(session.uuid, status)
    }

    suspend fun updateUrlLocation(session: Session, urlLocation: String?) {
        mDatabase.sessions().updateUrlLocation(session.uuid, urlLocation)
    }

    suspend fun updateSessionAveragingFrequency(sessionId: Long, averagingFrequency: Int) {
        mDatabase.sessions().updateAveragingFrequency(sessionId, averagingFrequency)
    }

    suspend fun updateFollowedAt(session: Session) {
        mDatabase.sessions().updateFollowedAt(session.uuid, session.followedAt)
    }

    suspend fun updateOrder(sessionUUID: String, order: Int?) {
        mDatabase.sessions().updateOrder(sessionUUID, order)
    }
}
