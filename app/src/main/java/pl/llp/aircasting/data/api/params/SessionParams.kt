package pl.llp.aircasting.data.api.params

import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.TAGS_SEPARATOR
import pl.llp.aircasting.util.DateConverter
import java.util.*

private val UTC = TimeZone.getTimeZone("UTC")

class SessionParams(session: Session) {
    companion object {
        const val MOBILE_SESSION_TYPE = "MobileSession"
        const val FIXED_SESSION_TYPE = "FixedSession"
    }

    data class AirBeamParams(val mac_address: String?, val model: String, val name: String)

    val uuid: String
    val type: String
    val title: String
    val tag_list: String
    val start_time: String
    val end_time: String
    val contribute: Boolean
    val is_indoor: Boolean
    val notes: List<NoteParams>
    val version: Int
    val streams = hashMapOf<String, MeasurementStreamParams>()

    val latitude: Double?
    val longitude: Double?
    val airbeam: AirBeamParams?

    init {
        this.uuid = session.uuid
        this.type = when(session.type) {
            Session.Type.FIXED -> FIXED_SESSION_TYPE
            Session.Type.MOBILE -> MOBILE_SESSION_TYPE
        }
        this.contribute = session.contribute
        this.title = session.name
        this.start_time = DateConverter.toDateString(session.startTime, UTC)
        this.end_time = DateConverter.toDateString(session.endTime ?: Date(), UTC)
        this.tag_list = session.tags.joinToString(TAGS_SEPARATOR)
        this.version = session.version
        this.is_indoor = session.indoor
        this.latitude = session.location?.latitude
        this.longitude = session.location?.longitude
        this.airbeam = if (session.type == Session.Type.FIXED && session.deviceId != null)
            AirBeamParams(
                mac_address = session.deviceId,
                model = "AirBeamMini",
                name = session.name,
            )
        else null
        session.streams.forEach { stream ->
            streams[stream.sensorName] =
                MeasurementStreamParams(stream)
        }
        notes = session.notes.map { note -> NoteParams(note) }
    }
}
