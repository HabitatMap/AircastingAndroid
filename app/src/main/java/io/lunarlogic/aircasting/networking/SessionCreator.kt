package io.lunarlogic.aircasting.networking

import android.content.Context
import android.widget.Toast
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.sensor.Session
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SessionCreator(private val mContext: Context, private val settings: Settings) {
    private val mErrorHandler = ErrorHandler(mContext)
    private val apiService = ApiServiceFactory.get(settings.getAuthToken()!!)

    fun create(session: Session) {
        val sessionParams = SessionParams(session)
        val sessionBody = CreateSessionBody(GzippedSession.get(sessionParams))
        val call = apiService.createSession(sessionBody)
        call.enqueue(object : Callback<Session> {
            override fun onResponse(call: Call<Session>, response: Response<Session>) {
                println(response.message())
                if (response.isSuccessful) {
                    val message = "Session created successfully! Check http://aircasting.habitatmap.org/mobile_map"
                    val toast = Toast.makeText(mContext, message, Toast.LENGTH_LONG)
                    toast.show()
                } else {
                    mErrorHandler.handleAndDisplay(UnexpectedAPIError())
                }
            }

            override fun onFailure(call: Call<Session>, t: Throwable) {
                mErrorHandler.handleAndDisplay(UnexpectedAPIError(t))
            }
        })
    }
}