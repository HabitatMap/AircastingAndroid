package pl.llp.aircasting.data.api.services

import android.content.Context
import com.google.gson.Gson
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.params.SessionParams
import pl.llp.aircasting.data.api.params.UpdateSessionBody
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.UnexpectedAPIError
import javax.inject.Inject
import javax.inject.Singleton

@UserSessionScope
class UpdateSessionService @Inject constructor(
    @Authenticated private val apiService: ApiService,
    private val errorHandler: ErrorHandler,
    private val context: Context
) {
    suspend fun update(session: Session): Result<Unit> = runCatching {
        val sessionParams = SessionParams(session)
        val gson = Gson()
        val jsonData = gson.toJson(sessionParams)
        val response = apiService.updateSession(UpdateSessionBody(jsonData))

        if (!response.isSuccessful) {
            throw UnexpectedAPIError()
        }
    }.onFailure { throwable ->
        errorHandler.handle(UnexpectedAPIError(throwable))
        errorHandler.showError(context.getString(R.string.errors_network_required_edit))
    }
}
