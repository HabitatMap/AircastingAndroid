package pl.llp.aircasting.data.local.repository

import android.util.Log
import androidx.lifecycle.LiveData
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.AppDatabase
import pl.llp.aircasting.data.local.entity.*
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import java.util.*
import javax.inject.Inject

@UserSessionScope
class SessionsRepository @Inject constructor(
    private val mDatabase: AppDatabase
) {
    suspend fun insert(session: Session): Long {
        val sessionDBObject =
            SessionDBObject(session)
        return mDatabase.sessions().insert(sessionDBObject)
    }

    suspend fun getMobileActiveSessionIdByDeviceId(deviceId: String): Long? {
        val sessionDBObject = mDatabase.sessions().loadSessionByDeviceIdStatusAndType(
            deviceId,
            Session.Status.RECORDING, Session.Type.MOBILE
        )

        return sessionDBObject?.id
    }

    suspend fun getSessionByUUID(uuid: String?): SessionDBObject? {
        uuid ?: return null

        return mDatabase.sessions().loadSessionByUUID(uuid)
    }

    suspend fun updateAveragingFrequency(sessionId: Long?, averagingFrequency: Int?) {
        sessionId ?: return
        averagingFrequency ?: return

        mDatabase.sessions().updateAveragingFrequency(sessionId, averagingFrequency)
    }

    suspend fun getSessionByIdSuspend(id: Long): SessionDBObject? {
        return mDatabase.sessions().loadSessionById(id)
    }

    suspend fun getSessionWithMeasurementsByUUID(uuid: String): SessionWithStreamsAndMeasurementsDBObject? {
        return mDatabase.sessions().loadSessionAndMeasurementsByUUID(uuid)
    }

    suspend fun getSessionIdByUUID(uuid: String): Long? {
        return mDatabase.sessions().loadSessionByUUID(uuid)?.id
    }

    suspend fun loadSessionAndMeasurementsByUUID(uuid: String): Session? {
        val sessionDBObject = mDatabase.sessions().loadSessionAndMeasurementsByUUID(uuid)

        sessionDBObject ?: return null
        return Session(sessionDBObject)
    }

    suspend fun loadSessionForUpload(uuid: String): Session? {
        val sessionForUploadDBObject = mDatabase.sessions().loadSessionForUploadByUUID(uuid)

        return sessionForUploadDBObject?.let { Session(it) }
    }

    suspend fun update(session: Session) {
        session.endTime?.let {
            mDatabase.sessions().update(
                session.uuid, session.name, session.tags,
                it, session.status, session.version, session.urlLocation
            )
        }
    }

    suspend fun mobileSessionAlreadyExistsForDeviceId(deviceId: String): Boolean {
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

    suspend fun isMicrophoneSessionAlreadyRecording(): Boolean {
        return mDatabase.sessions().loadSessionByStatusTypeAndDeviceType(
            Session.Status.RECORDING,
            Session.Type.MOBILE,
            DeviceItem.Type.MIC
        ) != null
    }

    suspend fun disconnectMobileBluetoothSessions() {
        mDatabase.sessions().disconnectSessions(
            Session.Status.DISCONNECTED,
            DeviceItem.Type.MIC,
            Session.Type.MOBILE,
            Session.Status.RECORDING
        )
    }

    suspend fun finishMobileMicSessions() {
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

    suspend fun fixedSessions(): List<SessionDBObject> {
        return mDatabase.sessions().byType(Session.Type.FIXED)
    }

    suspend fun followedSessions(): List<SessionDBObject> {
        return mDatabase.sessions().loadFollowed()
    }

    suspend fun sessionsIdsByType(type: Session.Type): List<Long> {
        return mDatabase.sessions().loadSessionUuidsByType(type)
    }

    suspend fun delete(uuids: List<String>) {
        if (uuids.isNotEmpty()) {
            mDatabase.sessions().delete(uuids)
        }
    }

    suspend fun delete(uuid: String) {
        mDatabase.sessions().delete(uuid)
    }

    suspend fun markForRemoval(uuid: String) {
        Log.d(TAG, "Marking $uuid for deletion")
        mDatabase.sessions().markForRemoval(uuid)
    }

    suspend fun updateOrCreate(session: Session): Long? {
        val sessionDbObject = mDatabase.sessions().loadSessionByUUID(session.uuid)
        return if (sessionDbObject == null) {
            insert(session)
        } else {
            update(session)
            null
        }
    }

    suspend fun updateSessionStatus(session: Session, status: Session.Status) {
        mDatabase.sessions().updateStatus(session.uuid, status)
    }

    suspend fun updateUrlLocation(session: Session, urlLocation: String?) {
        mDatabase.sessions().updateUrlLocation(session.uuid, urlLocation)
    }

    suspend fun updateFollowedAt(session: Session) {
        mDatabase.sessions().updateFollowedAt(session.uuid, session.followedAt)
    }

    fun loadMobileActiveSessionsWithMeasurementsList(): LiveData<List<SessionWithStreamsAndLastMeasurementsDBObject>> {
        return mDatabase.sessions().loadAllByTypeAndStatusWithLastMeasurements(
            Session.Type.MOBILE,
            listOf(Session.Status.RECORDING.value, Session.Status.DISCONNECTED.value)
        )
    }

    fun loadFixedSessionsWithMeasurements(): LiveData<List<SessionWithStreamsDBObject>> {
        return mDatabase.sessions().loadAllByType(Session.Type.FIXED)
    }

    fun loadFollowingSessionsWithMeasurements(): LiveData<List<SessionWithStreamsAndLastMeasurementsDBObject>> {
        return mDatabase.sessions().loadFollowingWithMeasurements()
    }

    fun loadLiveDataCompleteSessionBySessionUUID(sessionUUID: String): LiveData<CompleteSessionDBObject?> {
        return mDatabase.sessions().loadLiveDataSessionForUploadByUUID(sessionUUID)
    }

    fun loadSessionWithNotesAndStreamsByUUID(sessionUUID: String): LiveData<SessionWithStreamsAndNotesDBObject?> {
        return mDatabase.sessions().loadSessionWithNotesByUUID(sessionUUID)
    }

    fun loadMobileDormantSessionsWithMeasurementsAndNotes(): LiveData<List<SessionWithStreamsAndNotesDBObject>> {
        return mDatabase.sessions().loadAllByTypeAndStatusWithNotes(
            Session.Type.MOBILE,
            Session.Status.FINISHED
        )
    }

    suspend fun reloadSessionWithMeasurements(uuid: String): SessionWithStreamsAndMeasurementsDBObject? {
        return mDatabase.sessions().reloadSessionAndMeasurementsByUUID(uuid)
    }
}
