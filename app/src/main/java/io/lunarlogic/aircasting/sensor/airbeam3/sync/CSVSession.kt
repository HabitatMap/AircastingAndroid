package io.lunarlogic.aircasting.sensor.airbeam3.sync

import io.lunarlogic.aircasting.lib.DateConverter
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.sensor.airbeam3.sync.SDCardCSVFileFactory.Header
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CSVSession(val uuid: String, val streams: HashMap<Int, ArrayList<CSVMeasurement>> = HashMap()) {
    companion object {
        const val DEFAULT_NAME = "Imported from SD card"
        const val DATE_FORMAT = "MM/dd/yyyy HH:mm:ss"

        fun uuidFrom(line: Array<String>): String? {
            return line[Header.UUID.value]
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
        val latitude = getValueFor(line, Header.LATITUDE)
        val longitude = getValueFor(line, Header.LONGITUDE)
        val dateString = "${line[Header.DATE.value]} ${line[SDCardCSVFileFactory.Header.TIME.value]}"
        val time = DateConverter.fromString(dateString,
            DATE_FORMAT
        )

        val supportedStreamHeaders = CSVMeasurementStream.SUPPORTED_STREAMS.keys
        supportedStreamHeaders.forEach { streamHeader ->
            if (!streams.containsKey(streamHeader.value)) {
                streams[streamHeader.value] = ArrayList()
            }

            val measurement = buildMeasurement(latitude, longitude, time, line, streamHeader)
            measurement?.let { streams[streamHeader.value]?.add(measurement) }
        }
    }

    private fun buildMeasurement(
        latitude: Double?,
        longitude: Double?,
        time: Date?,
        line: Array<String>,
        streamHeader: Header
    ): CSVMeasurement? {
        time ?: return null

        val value = getValueFor(line, streamHeader) ?: return null

        return CSVMeasurement(
                value,
                latitude,
                longitude,
                time
            )
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

    private fun getValueFor(line: Array<String>, header: Header): Double? {
        try {
            return line[header.value].toDouble()
        } catch(e: NumberFormatException) {
            return null
        }
    }
}
