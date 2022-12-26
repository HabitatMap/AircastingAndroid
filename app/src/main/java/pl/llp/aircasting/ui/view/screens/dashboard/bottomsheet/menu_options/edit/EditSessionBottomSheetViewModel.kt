package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.edit

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.llp.aircasting.data.api.services.SessionDownloadService
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import javax.inject.Inject

class EditSessionBottomSheetViewModel @Inject constructor(
    private val mDownloadService: SessionDownloadService?,
    private val mSessionsRepository: SessionsRepository
) : ViewModel() {
    fun reload(session: Session?): Flow<Result<Session>> {
        session ?: return flow { emit(Result.failure(Exception("Session was null"))) }

        return flow {
            emit(
                mDownloadService?.download(session.uuid)
                    ?.onSuccess { mSessionsRepository.update(it) }
                    ?: Result.failure(Exception("User token was null"))
            )
        }
    }
}