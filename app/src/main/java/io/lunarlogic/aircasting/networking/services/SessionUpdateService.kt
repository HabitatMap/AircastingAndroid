package io.lunarlogic.aircasting.networking.services

import android.util.Log
import com.google.gson.Gson
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.SyncError
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.responses.SyncResponse
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.networking.GzippedSession
import io.lunarlogic.aircasting.networking.params.*
import io.lunarlogic.aircasting.networking.responses.SessionResponse
import io.lunarlogic.aircasting.networking.responses.UpdateSessionResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.atomic.AtomicBoolean

class SessionUpdateService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    fun update(session: Session) {
        val sessionParams = SessionParams(session)

        val gson = Gson()
        val jsonData = gson.toJson(sessionParams)
        val call = apiService.updateSession(UpdateSessionBody(jsonData))

        call.enqueue(object : Callback<UpdateSessionResponse> {
            override fun onResponse(call: Call<UpdateSessionResponse>, response: Response<UpdateSessionResponse>) {
                if (response.isSuccessful) {
                    println("HURRA! Session updated")
                    val body = response.body()
                    // TODO : update locally session version with the one returned in response
                    // If we update session locally, local version is higher than api version, so we should send and update in sync service
                } else {
                    errorHandler.handle(UnexpectedAPIError())
                }
            }

            override fun onFailure(call: Call<UpdateSessionResponse>, t: Throwable) {
                errorHandler.handle(UnexpectedAPIError(t))
            }
        })
    }

}
