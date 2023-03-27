package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.params.ForgotPasswordBody
import pl.llp.aircasting.data.api.params.ForgotPasswordParams
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.UnexpectedAPIError

class ForgotPasswordService(
    private val mErrorHandler: ErrorHandler,
    private val mApiServiceFactory: ApiServiceFactory
) {
    suspend fun resetPassword(login: String): Result<Unit> = runCatching {
        val apiService = mApiServiceFactory.get()
        val forgotPasswordParams = ForgotPasswordParams(login)
        val forgotPasswordBody = ForgotPasswordBody(forgotPasswordParams)
        val response = apiService.resetPassword(forgotPasswordBody)

        if (!response.isSuccessful) {
            throw UnexpectedAPIError()
        }
    }.onFailure { throwable ->
        mErrorHandler.handleAndDisplay(UnexpectedAPIError(throwable))
    }
}
