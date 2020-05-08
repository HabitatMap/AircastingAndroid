package io.lunarlogic.aircasting.networking

import io.lunarlogic.aircasting.sensor.Session
import java.util.*

class SessionParams {
    constructor(session: Session) {
        this.uuid = session.uuid
        this.title = session.name
        this.start_time = session.startTime
        this.end_time = session.endTime!!
        this.tag_list = session.tags

        session.streams.forEach {(sensorName, stream) ->
            streams[sensorName] = MeasurementStreamParams(stream)
        }
    }

    val uuid: UUID
    val title: String
    val tag_list: List<String>
    val start_time: Date
    val end_time: Date
    val calibration = 100 // handle
    val contribute = true // handle
    val drawable = 2_131_165_443 // handle
    val is_indoor = false // handle
    val latitude = 0.0 // handle
    val longitude = 0.0 // handle
    val deleted = false // handle
    val notes = listOf<String>() // handle
    val type = "MobileSession" // handle
    val version = 0 // handle
    val streams = hashMapOf<String, MeasurementStreamParams>()
}