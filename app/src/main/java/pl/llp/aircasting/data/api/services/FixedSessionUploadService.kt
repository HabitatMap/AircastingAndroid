package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.GzippedParams
import pl.llp.aircasting.data.api.params.CreateSessionBody
import pl.llp.aircasting.data.api.params.SessionParams
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.UnexpectedAPIError
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@UserSessionScope
class FixedSessionUploadService @Inject constructor(
    @Authenticated private val apiService: ApiService,
    private val errorHandler: ErrorHandler
) {
    suspend fun upload(session: Session): Result<Unit> = runCatching {
        session.endTime = Date()

        val sessionParams = SessionParams(session)
        val sessionBody =
            CreateSessionBody(GzippedParams.get(sessionParams, SessionParams::class.java))
        val response = apiService.createFixedSession(sessionBody)

        if (!response.isSuccessful) {
            throw UnexpectedAPIError()
        }
    }.onFailure { throwable ->
        errorHandler.handle(UnexpectedAPIError(throwable))
    }
}
