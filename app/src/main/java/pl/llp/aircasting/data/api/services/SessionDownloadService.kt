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
import java.util.TimeZone
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
        // BE stores start_time_local / end_time_local as wall-clock numerals treated as
        // UTC (TimeToLocalInUTC + skip_time_zone_conversion_for_attributes) and tags them
        // with a misleading "Z" suffix. The wall clock is the session's `time_zone` on
        // BE: mapped sessions get a lat/lng-derived TZ (≈ phone-default), indoor /
        // locationless sessions default to UTC. Parse with the matching TZ so Date.time
        // is the real instant for that wall clock.
        // For indoor sessions, we only parse as UTC if it's an AirBeamMini V2 (version >= 3).
        // Older models (AirBeamMini V1, AirBeam3, AirBeam2) write and upload local time numerals.
        val beTimeZone = TimeZone.getDefault()
        val startTime = DateConverter.fromString(sessionResponse.start_time, beTimeZone)
            ?: throw UnexpectedAPIError()

        val streams = sessionResponse.streams.values.map { stream ->
            MeasurementStream(stream)
        }

        val session = Session(
            uuid = sessionResponse.uuid,
            deviceId = null,
            deviceType = null,
            mType = sessionType(sessionResponse.type),
            mName = sessionResponse.title ?: "Unnamed session",
            mTags = ArrayList(sessionResponse.tag_list.split(TAGS_SEPARATOR)),
            mStatus = Session.Status.FINISHED,
            mStartTime = startTime,
            endTime = DateConverter.fromString(sessionResponse.end_time, beTimeZone),
            version = sessionResponse.version,
            deleted = sessionResponse.deleted,
            followedAt = null,
            contribute = sessionResponse.contribute,
            locationless = false,
            mIndoor = sessionResponse.is_indoor,
            mStreams = streams,
            urlLocation = sessionResponse.location
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
