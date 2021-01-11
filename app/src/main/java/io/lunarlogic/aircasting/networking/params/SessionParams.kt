package io.lunarlogic.aircasting.networking.params

import io.lunarlogic.aircasting.lib.DateConverter
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.TAGS_SEPARATOR
import io.lunarlogic.aircasting.sensor.microphone.DEFAULT_CALIBRATION

class SessionParams {
    companion object {
        val MOBILE_SESSION_TYPE = "MobileSession"
        val FIXED_SESSION_TYPE = "FixedSession"
    }

    constructor(session: Session) {
        this.uuid = session.uuid
        this.type = when(session.type) {
            Session.Type.FIXED -> FIXED_SESSION_TYPE
            Session.Type.MOBILE -> MOBILE_SESSION_TYPE
        }

        this.contribute = session.contribute
        this.title = session.name
        this.start_time = DateConverter.toDateString(session.startTime)
        this.end_time = DateConverter.toDateString(session.endTime!!)
        this.calibration = 100 // todo: JAK TO TUTAJ USTAWIC
        this.tag_list = session.tags.joinToString(TAGS_SEPARATOR)
        this.version = session.version
        this.is_indoor = session.indoor ?: false
        this.latitude = session.location?.latitude
        this.longitude = session.location?.longitude

        session.streams.forEach { stream ->
            streams[stream.sensorName] =
                MeasurementStreamParams(stream)
        }
    }

    val uuid: String
    val type: String
    val title: String
    val tag_list: String
    val start_time: String
    val end_time: String
//    val calibration = DEFAULT_CALIBRATION // TODO: handle for microphone session only
    val calibration: Int
    val contribute: Boolean
    val is_indoor: Boolean
    val notes = listOf<String>() // TODO: handle after adding notes
    val version: Int
    val streams = hashMapOf<String, MeasurementStreamParams>()

    val latitude: Double?
    val longitude: Double?
}
