package pl.llp.aircasting.data.local.repository

import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import java.util.*

class SessionsRepository {
    private val mDatabase = DatabaseProvider.get()

    fun insert(localSession: LocalSession): Long {
        val sessionDBObject =
            SessionDBObject(localSession)
        return mDatabase.sessions().insert(sessionDBObject)
    }

    fun getMobileActiveSessionIdByDeviceId(deviceId: String): Long? {
        val localSessionDBObject = mDatabase.sessions().loadSessionByDeviceIdStatusAndType(deviceId,
            LocalSession.Status.RECORDING, LocalSession.Type.MOBILE)

        if (localSessionDBObject != null) {
            return localSessionDBObject.id
        } else {
            return null
        }
    }

    fun getSessionByUUID(uuid: String): SessionDBObject? {
        return mDatabase.sessions().loadSessionByUUID(uuid)
    }

    fun getSessionById(id: Long): SessionDBObject? {
        return mDatabase.sessions().loadSessionById(id)
    }

    fun getSessionWithMeasurementsByUUID(uuid: String): SessionWithStreamsAndMeasurementsDBObject? {
        return mDatabase.sessions().loadSessionAndMeasurementsByUUID(uuid)
    }

    fun getSessionIdByUUID(uuid: String): Long? {
        return mDatabase.sessions().loadSessionByUUID(uuid)?.id
    }

    fun loadSessionAndMeasurementsByUUID(uuid: String): LocalSession? {
        val sessionDBObject = mDatabase.sessions().loadSessionAndMeasurementsByUUID(uuid)

        if (sessionDBObject != null) {
            return LocalSession(sessionDBObject)
        } else {
            return null
        }
    }

    fun loadSessionForUpload(uuid: String): LocalSession? {
        val sessionForUploadDBObject = mDatabase.sessions().loadSessionForUploadByUUID(uuid)

        if (sessionForUploadDBObject != null) {
            return LocalSession(sessionForUploadDBObject)
        } else {
            return null
        }

    }

    fun update(localSession: LocalSession) {
        localSession.endTime?.let {
            mDatabase.sessions().update(localSession.uuid, localSession.name, localSession.tags,
                it, localSession.status, localSession.version, localSession.urlLocation)
        }
    }

    fun mobileSessionAlreadyExistsForDeviceId(deviceId: String): Boolean {
        return mDatabase.sessions().loadSessionByDeviceIdStatusAndType(deviceId, LocalSession.Status.RECORDING, LocalSession.Type.MOBILE) != null ||
                mDatabase.sessions().loadSessionByDeviceIdStatusAndType(deviceId, LocalSession.Status.DISCONNECTED, LocalSession.Type.MOBILE) != null
    }

    fun isMicrophoneSessionAlreadyRecording(): Boolean {
        return mDatabase.sessions().loadSessionByStatusTypeAndDeviceType(LocalSession.Status.RECORDING, LocalSession.Type.MOBILE, DeviceItem.Type.MIC) != null
    }

    fun mobileActiveSessionExists(): Boolean {
        return mDatabase.sessions().loadSessionByStatusAndType(LocalSession.Status.RECORDING, LocalSession.Type.MOBILE) != null ||
                mDatabase.sessions().loadSessionByStatusAndType(LocalSession.Status.DISCONNECTED, LocalSession.Type.MOBILE) != null
    }

    fun disconnectMobileBluetoothSessions() {
        mDatabase.sessions().disconnectSessions(LocalSession.Status.DISCONNECTED, DeviceItem.Type.MIC, LocalSession.Type.MOBILE, LocalSession.Status.RECORDING)
    }

    fun finishMobileMicSessions() {
        mDatabase.sessions().finishSessions(LocalSession.Status.FINISHED, Date(), DeviceItem.Type.MIC, LocalSession.Type.MOBILE, LocalSession.Status.RECORDING)
    }

    fun disconnectSession(deviceId: String) {
        mDatabase.sessions().disconnectSession(LocalSession.Status.DISCONNECTED, deviceId, LocalSession.Status.RECORDING)
    }

    fun finishedSessions(): List<LocalSession> {
        return mDatabase.sessions().byStatus(LocalSession.Status.FINISHED)
            .map { dbObject -> LocalSession(dbObject) }
    }

    fun fixedSessions(): List<SessionDBObject> {
        return mDatabase.sessions().byType(LocalSession.Type.FIXED)
    }

    fun sessionsIdsByType(type: LocalSession.Type): List<Long> {
        return mDatabase.sessions().loadSessionUuidsByType(type)
    }

    fun delete(uuids: List<String>) {
        if (uuids.isNotEmpty()) {
            mDatabase.sessions().delete(uuids)
        }
    }

    fun markForRemoval(uuids: List<String>) {
        mDatabase.sessions().markForRemoval(uuids)
    }

    fun deleteMarkedForRemoval() {
        mDatabase.sessions().deleteMarkedForRemoval()
    }

    fun updateOrCreate(localSession: LocalSession): Long? {
        val sessionDbObject = mDatabase.sessions().loadSessionByUUID(localSession.uuid)
        return if (sessionDbObject == null) {
            insert(localSession)
        } else {
            update(localSession)
            null
        }
    }

    fun updateSessionStatus(localSession: LocalSession, status: LocalSession.Status) {
        mDatabase.sessions().updateStatus(localSession.uuid, status)
    }

    fun updateUrlLocation(localSession: LocalSession, urlLocation: String?) {
        mDatabase.sessions().updateUrlLocation(localSession.uuid, urlLocation)
    }

    fun updateSessionAveragingFrequency(sessionId: Long, averagingFrequency: Int) {
        mDatabase.sessions().updateAveragingFrequency(sessionId, averagingFrequency)
    }

}
