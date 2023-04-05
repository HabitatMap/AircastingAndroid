package pl.llp.aircasting.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.flow
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import javax.inject.Inject

class AddNoteBottomSheetViewModel @Inject constructor(
    private val measurementsRepository: MeasurementsRepositoryImpl,
    private val sessionsRepository: SessionsRepository,
) : ViewModel() {
    fun lastAveragedMeasurementTime(uuid: String?) = flow {
        val session = sessionsRepository.getSessionByUUID(uuid)
        session ?: return@flow

        emit(
            measurementsRepository.lastTimeOfMeasurementWithAveragingFrequency(
                session.id,
                session.averagingFrequency
            )
        )
    }

    fun getSessionByUUID(uuid: String?) = flow {
        val sessionDB = sessionsRepository.loadCompleteSession(uuid) ?: return@flow
        emit(sessionDB)
    }
}