package pl.llp.aircasting.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.entity.*
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.local.repository.ThresholdsRepository
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.Session

class SessionsViewModel : ViewModel() {
    private val mDatabase = DatabaseProvider.get()
    private val thresholdsRepository = ThresholdsRepository()
    private val sessionsRepository = SessionsRepository()

    fun loadSessionWithMeasurements(uuid: String): LiveData<SessionWithStreamsAndMeasurementsDBObject?> {
        return mDatabase.sessions().loadLiveDataSessionAndMeasurementsByUUID(uuid)
    }

    fun reloadSessionWithMeasurements(uuid: String): SessionWithStreamsAndMeasurementsDBObject? {
        return mDatabase.sessions().reloadSessionAndMeasurementsByUUID(uuid)
    }

    fun loadFollowingSessionsWithMeasurements(): LiveData<List<SessionWithStreamsAndLastMeasurementsDBObject>> {
        return mDatabase.sessions().loadFollowingWithMeasurements()
    }

    fun loadMobileActiveSessionsWithMeasurements(): LiveData<List<SessionWithStreamsAndLastMeasurementsDBObject>> {
        return mDatabase.sessions().loadAllByTypeAndStatusWithLastMeasurements(
            Session.Type.MOBILE,
            listOf(Session.Status.RECORDING.value, Session.Status.DISCONNECTED.value)
        )
    }

    fun loadLiveDataCompleteSessionBySessionUUID(sessionUUID: String): LiveData<CompleteSessionDBObject?> {
        return mDatabase.sessions().loadLiveDataSessionForUploadByUUID(sessionUUID)
    }

    fun loadSessionWithNotesAndStreamsByUUID(sessionUUID: String): LiveData<SessionWithStreamsAndNotesDBObject?> {
        return mDatabase.sessions().loadSessionWithNotesByUUID(sessionUUID)
    }

    fun loadMobileDormantSessionsWithMeasurements(): LiveData<List<SessionWithStreamsDBObject>> {
        return mDatabase.sessions().loadAllByTypeAndStatus(
            Session.Type.MOBILE,
            Session.Status.FINISHED
        )
    }

    fun loadMobileDormantSessionsWithMeasurementsAndNotes(): LiveData<List<SessionWithStreamsAndNotesDBObject>> {
        return mDatabase.sessions().loadAllByTypeAndStatusWithNotes(
            Session.Type.MOBILE,
            Session.Status.FINISHED
        )
    }

    fun loadFixedSessionsWithMeasurements(): LiveData<List<SessionWithStreamsDBObject>> {
        return mDatabase.sessions().loadAllByType(Session.Type.FIXED)
    }

    fun findOrCreateSensorThresholds(session: Session): List<SensorThreshold> {
        return thresholdsRepository.findOrCreateSensorThresholds(session)
    }

    fun findOrCreateSensorThresholds(streams: List<MeasurementStream>): List<SensorThreshold> {
        return thresholdsRepository.findOrCreateSensorThresholds(streams)
    }

    fun updateSensorThreshold(sensorThreshold: SensorThreshold) {
        thresholdsRepository.updateSensorThreshold(sensorThreshold)
    }

    fun updateFollowedAt(
        session: Session,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ) = viewModelScope.launch(ioDispatcher) {
        sessionsRepository.updateFollowedAt(session)
    }


    fun updateOrder(
        sessionUUID: String,
        followingSessionsNumber: Int,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ) = viewModelScope.launch(ioDispatcher) {
        sessionsRepository.updateOrder(sessionUUID, followingSessionsNumber)
    }
}