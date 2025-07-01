package pl.llp.aircasting.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.flow
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import javax.inject.Inject

class ShareSessionBottomSheetViewModel @Inject constructor(
    private val measurementsRepository: MeasurementsRepositoryImpl,
    private val sessionsRepository: SessionsRepository,
) : ViewModel() {
    fun reloadSessionWithMeasurements(uuid: String) = flow {
        emit(sessionsRepository.reloadSessionWithMeasurements(uuid))
    }
}