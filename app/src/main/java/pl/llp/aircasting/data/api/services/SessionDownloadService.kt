package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.UnexpectedAPIError
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.NoteResponseParser
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.TAGS_SEPARATOR
import pl.llp.aircasting.data.api.params.SessionParams
import pl.llp.aircasting.data.api.responses.SessionResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SessionDownloadService(
    private val apiService: ApiService,
    private val errorHandler: ErrorHandler
) {

    private val noteResponseParser = NoteResponseParser(errorHandler)

    fun download(
        uuid: String,
        successCallback: (Session) -> Unit?,
        finallyCallback: (() -> Unit?)? = null
    ) {
        val call = apiService.downloadSession(uuid)
        call.enqueue(object : Callback<SessionResponse> {
            override fun onResponse(
                call: Call<SessionResponse>,
                response: Response<SessionResponse>
            ) {
                if (response.isSuccessful) response.body()?.let {
                    val session = sessionFromResponse(it)
                    session?.let { successCallback(session) }
                } else errorHandler.handle(UnexpectedAPIError())

                finallyCallback?.invoke()
            }

            override fun onFailure(call: Call<SessionResponse>, t: Throwable) {
                errorHandler.handle(UnexpectedAPIError(t))
                finallyCallback?.invoke()
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
            sessionResponse.is_indoor,
            streams,
            sessionResponse.location
        )

        if (sessionResponse.latitude != null && sessionResponse.longitude != null) {
            session.location = Session.Location(sessionResponse.latitude, sessionResponse.longitude)
        }

        session.notes = sessionResponse.notes.map { noteResponse ->
            noteResponseParser.noteFromResponse(noteResponse)
        }.toMutableList()

        return session
    }

    private fun sessionType(type: String): Session.Type {
        return when (type) {
            SessionParams.FIXED_SESSION_TYPE -> Session.Type.FIXED
            else -> return Session.Type.MOBILE
        }
    }
}