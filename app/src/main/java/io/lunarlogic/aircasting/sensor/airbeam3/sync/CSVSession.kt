package io.lunarlogic.aircasting.sensor.airbeam3.sync

import io.lunarlogic.aircasting.lib.DateConverter
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CSVSession(val uuid: String, val streams: HashMap<Int, ArrayList<CSVMeasurement>> = HashMap()) {
    companion object {
        const val DEFAULT_NAME = "Imported from SD card"
        const val DATE_FORMAT = "MM/dd/yyyy HH:mm:ss"

        fun uuidFrom(line: Array<String>): String? {
            return line[SDCardCSVFileFactory.Header.UUID.value]
        }
    }

    fun startTime(): Date? {
        return firstMeasurement()?.time
    }

    fun latitude(): Double? {
        return firstMeasurement()?.latitude
    }

    fun longitude(): Double? {
        return firstMeasurement()?.longitude
    }

    private fun firstMeasurement(): CSVMeasurement? {
        // all streams are saved at the same time, so it does not matter which we take
        val stream = streams.values.firstOrNull()
        return stream?.firstOrNull()
    }

    fun addMeasurements(line: Array<String>) {
        val latitude = line[SDCardCSVFileFactory.Header.LATITUDE.value].toDouble() // TODO: handle parse issues
        val longitude = line[SDCardCSVFileFactory.Header.LONGITUDE.value].toDouble() // TODO: handle parse issues
        val dateString = "${line[SDCardCSVFileFactory.Header.DATE.value]} ${line[SDCardCSVFileFactory.Header.TIME.value]}"
        val time = DateConverter.fromString(dateString,
            DATE_FORMAT
        )

        time ?: return

        val supportedStreamHeaders = CSVMeasurementStream.SUPPORTED_STREAMS.keys
        supportedStreamHeaders.forEach { streamHeader ->
            val value = line[streamHeader.value].toDouble() // TODO: handle parse issues

            if (!streams.containsKey(streamHeader.value)) {
                streams[streamHeader.value] = ArrayList()
            }

            val measurement =
                CSVMeasurement(
                    value,
                    latitude,
                    longitude,
                    time
                )
            streams[streamHeader.value]?.add(measurement)
        }
    }

    fun toSession(deviceId: String): Session? {
        val startTime = startTime() ?: return null

        val session = Session(
            uuid,
            deviceId,
            DeviceItem.Type.AIRBEAM3,
            Session.Type.MOBILE,
            DEFAULT_NAME,
            ArrayList(),
            Session.Status.DISCONNECTED,
            startTime
        )

        val latitude = latitude()
        val longitude = longitude()
        if (latitude != null && longitude != null) {
            val location = Session.Location(latitude, longitude)
            session.location = location

            if (location == Session.Location.INDOOR_FAKE_LOCATION) {
                session.locationless = true
            }
        }

        return session
    }
}
