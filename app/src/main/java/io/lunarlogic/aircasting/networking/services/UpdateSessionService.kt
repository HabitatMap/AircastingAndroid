package io.lunarlogic.aircasting.networking.services

import android.content.Context
import com.google.gson.Gson
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.networking.params.SessionParams
import io.lunarlogic.aircasting.networking.params.UpdateSessionBody
import io.lunarlogic.aircasting.networking.responses.UpdateSessionResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateSessionService(private val apiService: ApiService, private val errorHandler: ErrorHandler, private val context: Context) {
    fun update(session: Session, successCallback: (() -> Unit)) {
        val sessionParams = SessionParams(session)

        val gson = Gson()
        val jsonData = gson.toJson(sessionParams)
        val call = apiService.updateSession(UpdateSessionBody(jsonData))

        call.enqueue(object : Callback<UpdateSessionResponse> {
            override fun onResponse(call: Call<UpdateSessionResponse>, response: Response<UpdateSessionResponse>) {
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
