package pl.llp.aircasting.data.api.params

import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.data.model.TAGS_SEPARATOR

class SessionParams(localSession: LocalSession) {
    companion object {
        const val MOBILE_SESSION_TYPE = "MobileSession"
        const val FIXED_SESSION_TYPE = "FixedSession"
    }

    val uuid: String
    val type: String
    val title: String
    val tag_list: String
    val start_time: String
    val end_time: String?
    val contribute: Boolean
    val is_indoor: Boolean
    val notes: List<NoteParams>
    val version: Int
    val streams = hashMapOf<String, MeasurementStreamParams>()

    val latitude: Double?
    val longitude: Double?

    init {
        this.uuid = localSession.uuid
        this.type = when(localSession.type) {
            LocalSession.Type.FIXED -> FIXED_SESSION_TYPE
            LocalSession.Type.MOBILE -> MOBILE_SESSION_TYPE
        }
        this.contribute = localSession.contribute
        this.title = localSession.name
        this.start_time = DateConverter.toDateString(localSession.startTime)
        this.end_time = localSession.endTime?.let { DateConverter.toDateString(it) }
        this.tag_list = localSession.tags.joinToString(TAGS_SEPARATOR)
        this.version = localSession.version
        this.is_indoor = localSession.indoor
        this.latitude = localSession.location?.latitude
        this.longitude = localSession.location?.longitude
        localSession.streams.forEach { stream ->
            streams[stream.sensorName] =
                MeasurementStreamParams(stream)
        }
        notes = localSession.notes.map { note -> NoteParams(note) }
    }
}
