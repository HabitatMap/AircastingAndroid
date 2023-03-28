package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.params.SessionParams
import pl.llp.aircasting.data.api.response.SessionResponse
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.TAGS_SEPARATOR
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.NoteResponseParser
import pl.llp.aircasting.util.exceptions.UnexpectedAPIError
import javax.inject.Inject

@UserSessionScope
class SessionDownloadService @Inject constructor(
    @Authenticated private val apiService: ApiService,
    private val noteResponseParser: NoteResponseParser,
) {

    suspend fun download(
        uuid: String,
    ): Result<Session> = runCatching { sessionFromResponse(apiService.downloadSession(uuid)) }

    private fun sessionFromResponse(sessionResponse: SessionResponse): Session {
        val startTime =
            DateConverter.fromString(sessionResponse.start_time) ?: throw UnexpectedAPIError()

        val streams = sessionResponse.streams.values.map { stream ->
            MeasurementStream(stream)
        }

        val session = Session(
            sessionResponse.uuid,
            null,
            null,
            sessionType(sessionResponse.type),
            sessionResponse.title ?: "Unnamed session",
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
