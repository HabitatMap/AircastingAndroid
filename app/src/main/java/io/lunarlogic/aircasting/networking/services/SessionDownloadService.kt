package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
import io.lunarlogic.aircasting.lib.DateConverter
import io.lunarlogic.aircasting.networking.params.SessionParams
import io.lunarlogic.aircasting.networking.responses.SessionResponse
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.TAGS_SEPARATOR
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SessionDownloadService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    fun download(uuid: String, successCallback: (Session) -> Unit?, finallyCallback: (() -> Unit?)? = null) {
        val call = apiService.downloadSession(uuid)
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
            sessionResponse.uuid,
            null,
            null,
            sessionType(sessionResponse.type),
            sessionResponse.title,
            ArrayList(sessionResponse.tag_list.split(TAGS_SEPARATOR)),
            Session.Status.FINISHED,
            startTime,
            DateConverter.fromString(sessionResponse.end_time),
            sessionResponse.version,
            sessionResponse.deleted,
            null,
            sessionResponse.contribute,
            false,
            streams,
            sessionResponse.location
        )

        if (sessionResponse.latitude != null && sessionResponse.longitude != null) {
            session.location = Session.Location(sessionResponse.latitude, sessionResponse.longitude)
        }

        return session
    }

    private fun sessionType(type: String): Session.Type {
        return when(type) {
            SessionParams.FIXED_SESSION_TYPE -> Session.Type.FIXED
            else -> return Session.Type.MOBILE
        }
    }
}
