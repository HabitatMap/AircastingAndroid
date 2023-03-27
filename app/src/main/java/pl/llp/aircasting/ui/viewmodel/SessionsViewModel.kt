package pl.llp.aircasting.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import pl.llp.aircasting.data.local.entity.CompleteSessionDBObject
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndLastMeasurementsDBObject
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndNotesDBObject
import pl.llp.aircasting.data.local.entity.SessionWithStreamsDBObject
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.local.repository.ThresholdsRepository
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.Session

class SessionsViewModel(
    private val thresholdsRepository: ThresholdsRepository = ThresholdsRepository(),
    private val sessionsRepository: SessionsRepository = SessionsRepository(),
) : ViewModel() {

    fun reloadSessionWithMeasurements(uuid: String) = flow {
        emit(sessionsRepository.reloadSessionWithMeasurements(uuid))
    }

    fun loadFollowingSessionsWithMeasurements(): LiveData<List<SessionWithStreamsAndLastMeasurementsDBObject>> {
        return sessionsRepository.loadFollowingSessionsWithMeasurements()
    }

    fun loadLiveDataCompleteSessionBySessionUUID(sessionUUID: String): LiveData<CompleteSessionDBObject?> {
        return sessionsRepository.loadLiveDataCompleteSessionBySessionUUID(sessionUUID)
    }

    fun loadSessionWithNotesAndStreamsByUUID(sessionUUID: String): LiveData<SessionWithStreamsAndNotesDBObject?> {
        return sessionsRepository.loadSessionWithNotesAndStreamsByUUID(sessionUUID)
    }

    fun loadMobileDormantSessionsWithMeasurementsAndNotes(): LiveData<List<SessionWithStreamsAndNotesDBObject>> {
        return sessionsRepository.loadMobileDormantSessionsWithMeasurementsAndNotes()
    }

    fun loadFixedSessionsWithMeasurements(): LiveData<List<SessionWithStreamsDBObject>> {
        return sessionsRepository.loadFixedSessionsWithMeasurements()
    }

    fun loadMobileActiveSessionsWithMeasurements(): LiveData<List<SessionWithStreamsAndLastMeasurementsDBObject>> {
        return sessionsRepository.loadMobileActiveSessionsWithMeasurementsList()
    }

    fun findOrCreateSensorThresholds(session: Session) = flow {
        emit(thresholdsRepository.findOrCreateSensorThresholds(session))
    }

    fun findOrCreateSensorThresholds(streams: List<MeasurementStream>) = flow {
        emit(thresholdsRepository.findOrCreateSensorThresholds(streams))
    }

    fun updateSensorThreshold(sensorThreshold: SensorThreshold) = viewModelScope.launch {
        thresholdsRepository.updateSensorThreshold(sensorThreshold)
    }

    fun updateFollowedAt(
        session: Session,
    ) = viewModelScope.launch {
        sessionsRepository.updateFollowedAt(session)
    }
}