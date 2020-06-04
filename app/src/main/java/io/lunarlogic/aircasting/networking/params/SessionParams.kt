package io.lunarlogic.aircasting.networking.params

import io.lunarlogic.aircasting.lib.DateConverter
import io.lunarlogic.aircasting.sensor.Session
import io.lunarlogic.aircasting.sensor.TAGS_SEPARATOR
import java.util.*

class SessionParams {
    constructor(session: Session) {
        this.uuid = session.uuid
        this.title = session.name
        this.start_time = DateConverter.toUTCDateString(session.startTime)
        this.end_time = DateConverter.toUTCDateString(session.endTime!!)
        this.tag_list = session.tags.joinToString(TAGS_SEPARATOR)

        session.streams.forEach { stream ->
            streams[stream.sensorName!!] =
                MeasurementStreamParams(stream)
        }
    }

    val uuid: String
    val title: String
    val tag_list: String
    val start_time: String
    val end_time: String
    val calibration = 100 // handle
    val contribute = true // handle
    val drawable = 2_131_165_443 // handle
    val is_indoor = false // handle
    val latitude = 0.0 // handle
    val longitude = 0.0 // handle
    val deleted = false
    val notes = listOf<String>() // handle
    val type = "MobileSession" // handle
    val version = 0
    val streams = hashMapOf<String, MeasurementStreamParams>()
}