package pl.llp.aircasting.ui.view.screens.login

import pl.llp.aircasting.data.api.response.UserResponse
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.InternalAPIError
import pl.llp.aircasting.util.exceptions.UnauthorizedError
import pl.llp.aircasting.util.exceptions.UnexpectedAPIError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginService @Inject constructor(
    val mSettings: Settings,
    private val mErrorHandler: ErrorHandler,
    private val mApiServiceFactory: ApiServiceFactory
) {
    suspend fun performLogin(
        username: String,
        password: String
    ): Result<UserResponse> = runCatching {
        val apiService = mApiServiceFactory.getAuthenticatedWithCredentials(username, password)
        val response = apiService.login()

        if (response.isSuccessful) {
            response.body() ?: throw InternalAPIError()
        } else if (response.code() == 401) {
            throw UnauthorizedError()
        } else {
            throw InternalAPIError()
        }
    }.onFailure { throwable ->
        mErrorHandler.handleAndDisplay(UnexpectedAPIError(throwable))
    }

    suspend fun getUser(): UserResponse? {
        return try {
            val response = mApiServiceFactory.getAuthenticated(mSettings.getAuthToken()).login()
            if (!response.isSuccessful) {
                mErrorHandler.handleAndDisplay(InternalAPIError())
                return null
            }

            response.body()
        } catch (e: Exception) {
            mErrorHandler.handleAndDisplay(UnexpectedAPIError())
            null
        }
    }
}
