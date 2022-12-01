package pl.llp.aircasting.data.api.services

import android.content.Context
import com.google.gson.Gson
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.params.SessionParams
import pl.llp.aircasting.data.api.params.UpdateSessionBody
import pl.llp.aircasting.data.api.response.UpdateSessionResponse
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.UnexpectedAPIError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateSessionService(
    private val apiService: ApiService,
    private val errorHandler: ErrorHandler,
    private val context: Context
) {
    fun update(session: Session, successCallback: (() -> Unit)) {
        val sessionParams = SessionParams(session)

        val gson = Gson()
        val jsonData = gson.toJson(sessionParams)
        val call = apiService.updateSession(UpdateSessionBody(jsonData))

        call.enqueue(object : Callback<UpdateSessionResponse> {
            override fun onResponse(
                call: Call<UpdateSessionResponse>,
                response: Response<UpdateSessionResponse>
            ) {
                if (response.isSuccessful) {
                    successCallback.invoke()
                } else {
                    errorHandler.handle(UnexpectedAPIError())
                    errorHandler.showError(context.getString(R.string.errors_edit_failure))
                }
            }

            override fun onFailure(call: Call<UpdateSessionResponse>, t: Throwable) {
                errorHandler.handle(UnexpectedAPIError(t))
                errorHandler.showError(context.getString(R.string.errors_network_required_edit))
            }
        })
    }

}
