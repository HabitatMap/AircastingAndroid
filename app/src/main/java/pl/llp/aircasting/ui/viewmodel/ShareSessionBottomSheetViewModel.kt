package pl.llp.aircasting.ui.viewmodel

import androidx.lifecycle.ViewModel
import pl.llp.aircasting.data.local.repository.SessionsRepository
import javax.inject.Inject

class ShareSessionBottomSheetViewModel @Inject constructor(
    private val sessionsRepository: SessionsRepository,
) : ViewModel() {
    suspend fun reloadSessionWithMeasurements(uuid: String) =
        sessionsRepository.reloadSessionWithMeasurements(uuid)

    suspend fun getSessionUrlLocation(uuid: String) =
        sessionsRepository.getUrlLocation(uuid)
}