package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
import io.lunarlogic.aircasting.lib.DateConverter
import io.lunarlogic.aircasting.networking.params.SessionParams
import io.lunarlogic.aircasting.networking.responses.SessionResponse
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session
import io.lunarlogic.aircasting.sensor.TAGS_SEPARATOR
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SessionDownloadService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    fun download(uuid: String, successCallback: (Session) -> Unit) {
        val call = apiService.show(uuid)
        call.enqueue(object : Callback<SessionResponse> {
            override fun onResponse(call: Call<SessionResponse>, response: Response<SessionResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()

                    body?.let {
                        val session = sessionFromResponse(body)
                        session?.let { successCallback(session) }
                    }
                } else {
                    errorHandler.handle(UnexpectedAPIError())
                }
            }

            override fun onFailure(call: Call<SessionResponse>, t: Throwable) {
                errorHandler.handle(UnexpectedAPIError(t))
            }
        })
    }

    fun sessionFromResponse(sessionResponse: SessionResponse): Session? {
        val startTime = DateConverter.fromString(sessionResponse.start_time) ?: return null

        val streams = sessionResponse.streams.values.map { stream ->
            MeasurementStream(stream)
        }

        val session = Session(
            null,
            sessionType(sessionResponse.type),
            sessionResponse.title,
            ArrayList(sessionResponse.tag_list.split(TAGS_SEPARATOR)),
            Session.Status.FINISHED,
            startTime,
            DateConverter.fromString(sessionResponse.end_time),
            sessionResponse.uuid,
            sessionResponse.version,
            sessionResponse.deleted,
            streams
        )

        return session
    }

    private fun sessionType(type: String): Session.Type {
        return when(type) {
            SessionParams.FIXED_SESSION_TYPE -> Session.Type.FIXED
            else -> return Session.Type.MOBILE
        }
    }
}
