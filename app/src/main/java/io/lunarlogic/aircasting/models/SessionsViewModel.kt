package io.lunarlogic.aircasting.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.*

class SessionsViewModel(): ViewModel() {
    private val mDatabase = DatabaseProvider.get()

    fun loadSessionWithMeasurements(uuid: String): LiveData<SessionWithStreamsAndMeasurementsDBObject?> {
        return mDatabase.sessions().loadLiveDataSessionAndMeasurementsByUUID(uuid)
    }

    fun reloadSessionWithMeasurements(uuid: String): SessionWithStreamsAndMeasurementsDBObject? {
        return mDatabase.sessions().reloadSessionAndMeasurementsByUUID(uuid)
    }

    fun loadFollowingSessionsWithMeasurements(): LiveData<List<SessionWithStreamsAndMeasurementsDBObject>> {
        return mDatabase.sessions().loadFollowingWithMeasurements()
    }

    fun loadMobileActiveSessionsWithMeasurements(): LiveData<List<SessionWithStreamsAndMeasurementsDBObject>> {
        return mDatabase.sessions().loadAllByTypeAndStatusWithMeasurements(
            Session.Type.MOBILE, listOf(Session.Status.RECORDING.value, Session.Status.DISCONNECTED.value))
    }

    // TODO: method to load notes for session with UUID
    fun loadNotesForSessionWithSessionId(sessionId: Long): LiveData<List<NoteDBObject?>> {
        return mDatabase.notes().loadLiveDataNotesBySessionId(sessionId)
    }

    fun loadMobileDormantSessionsWithMeasurements(): LiveData<List<SessionWithStreamsDBObject>> {
        return mDatabase.sessions().loadAllByTypeAndStatus(Session.Type.MOBILE, Session.Status.FINISHED)
    }

    fun loadFixedSessionsWithMeasurements(): LiveData<List<SessionWithStreamsDBObject>> {
        return mDatabase.sessions().loadAllByType(Session.Type.FIXED)
    }

    fun findOrCreateSensorThresholds(session: Session): List<SensorThreshold> {
        return findOrCreateSensorThresholds(session.streams)
    }

    fun findOrCreateSensorThresholds(streams: List<MeasurementStream>): List<SensorThreshold> {
        val existingThresholds = findSensorThresholds(streams)
        var newThresholds = createSensorThresholds(streams, existingThresholds)

        val thresholds = mutableListOf<SensorThreshold>()
        thresholds.addAll(existingThresholds)
        thresholds.addAll(newThresholds)

        return thresholds
    }

    private fun findSensorThresholds(streams: List<MeasurementStream>): List<SensorThreshold> {
        val sensorNames = streams.map { it.sensorName }
        return mDatabase.sensorThresholds()
            .allBySensorNames(sensorNames)
            .map { SensorThreshold(it) }
    }

    private fun createSensorThresholds(streams: List<MeasurementStream>, existingThreshols: List<SensorThreshold>): List<SensorThreshold> {
        val existingSensorNames = existingThreshols.map { it.sensorName }
        val toCreate = streams.filter { !existingSensorNames.contains(it.sensorName) }

        return toCreate.map { stream ->
            val sensorThresholdDBObject = SensorThresholdDBObject(stream)
            mDatabase.sensorThresholds().insert(sensorThresholdDBObject)
            SensorThreshold(sensorThresholdDBObject)
        }
    }

    fun updateSensorThreshold(sensorThreshold: SensorThreshold) {
        mDatabase.sensorThresholds().update(
            sensorThreshold.sensorName,
            sensorThreshold.thresholdVeryLow,
            sensorThreshold.thresholdLow,
            sensorThreshold.thresholdMedium,
            sensorThreshold.thresholdHigh,
            sensorThreshold.thresholdVeryHigh
        )
    }

    fun updateFollowedAt(session: Session) {
        mDatabase.sessions().updateFollowedAt(session.uuid, session.followedAt)
    }
}
