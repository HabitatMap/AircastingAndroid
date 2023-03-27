package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SessionExportFailedError
import pl.llp.aircasting.util.exceptions.SessionUploadPendingError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportSessionService @Inject constructor(
    @Authenticated private val apiService: ApiService,
    private val errorHandler: ErrorHandler
) {
    suspend fun export(email: String, uuid: String): Result<Unit> = runCatching {
        val response = apiService.exportSession(email, uuid)

        if (!response.isSuccessful) {
            throw SessionUploadPendingError()
        }
    }.onFailure { throwable ->
        errorHandler.handleAndDisplay(SessionExportFailedError(throwable))
    }
}
