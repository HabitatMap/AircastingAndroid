package io.lunarlogic.aircasting.networking.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.CreateSessionResponse
import io.lunarlogic.aircasting.networking.GzippedSession
import io.lunarlogic.aircasting.networking.params.CreateSessionBody
import io.lunarlogic.aircasting.networking.params.SessionParams
import io.lunarlogic.aircasting.sensor.Session
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SessionCreator(private val mContext: Context, private val settings: Settings) {
    private val mErrorHandler = ErrorHandler(mContext)
    private val apiService = ApiServiceFactory.get(settings.getAuthToken()!!)

    fun create(session: Session) {
        val sessionParams = SessionParams(session)
        val sessionBody = CreateSessionBody(
            GzippedSession.get(sessionParams)
        )
        val call = apiService.createSession(sessionBody)
        call.enqueue(object : Callback<CreateSessionResponse> {
            override fun onResponse(call: Call<CreateSessionResponse>, response: Response<CreateSessionResponse>) {
                println(response.message())
                if (response.isSuccessful) {
                    val message = "Session created successfully! Check http://aircasting.habitatmap.org/mobile_map"
                    val toast = Toast.makeText(mContext, message, Toast.LENGTH_LONG)
                    toast.show()

                    val sessionResponse = response.body()
                    val uri = Uri.parse(sessionResponse?.location)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    mContext.startActivity(intent)
                } else {
                    mErrorHandler.handleAndDisplay(UnexpectedAPIError())
                }
            }

            override fun onFailure(call: Call<CreateSessionResponse>, t: Throwable) {
                mErrorHandler.handleAndDisplay(UnexpectedAPIError(t))
            }
        })
    }
}