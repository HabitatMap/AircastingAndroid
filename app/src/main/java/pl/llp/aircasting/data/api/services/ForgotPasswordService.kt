package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.params.ForgotPasswordBody
import pl.llp.aircasting.data.api.params.ForgotPasswordParams
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.UnexpectedAPIError
import javax.inject.Inject

class ForgotPasswordService @Inject constructor(
    private val errorHandler: ErrorHandler,
    @NonAuthenticated private val apiService: ApiService,
) {
    suspend fun resetPassword(login: String): Result<Unit> = runCatching {
        val forgotPasswordParams = ForgotPasswordParams(login)
        val forgotPasswordBody = ForgotPasswordBody(forgotPasswordParams)
        val response = apiService.resetPassword(forgotPasswordBody)

        if (!response.isSuccessful) {
            throw UnexpectedAPIError()
        }
    }.onFailure { throwable ->
        errorHandler.handleAndDisplay(UnexpectedAPIError(throwable))
    }
}
