package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.DateConverter
import java.util.*

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
        val latitude = getValueFor(line, SDCardCSVFileFactory.Header.LATITUDE)
        val longitude = getValueFor(line, SDCardCSVFileFactory.Header.LONGITUDE)
        val dateString = "${line[SDCardCSVFileFactory.Header.DATE.value]} ${line[SDCardCSVFileFactory.Header.TIME.value]}"
        val time = DateConverter.fromString(
            dateString,
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
        streamHeader: SDCardCSVFileFactory.Header
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

    fun toSession(deviceId: String): LocalSession? {
        val startTime = startTime() ?: return null

        val localSession = LocalSession(
            uuid,
            deviceId,
            DeviceItem.Type.AIRBEAM3,
            LocalSession.Type.MOBILE,
            DEFAULT_NAME,
            ArrayList(),
            LocalSession.Status.DISCONNECTED,
            startTime
        )

        val latitude = latitude()
        val longitude = longitude()
        if (latitude != null && longitude != null) {
            val location = LocalSession.Location(latitude, longitude)
            localSession.location = location

            if (location == LocalSession.Location.FAKE_LOCATION) {
                localSession.locationless = true
            }
        }

        return localSession
    }

    private fun getValueFor(line: Array<String>, header: SDCardCSVFileFactory.Header): Double? {
        try {
            return line[header.value].toDouble()
        } catch(e: NumberFormatException) {
            return null
        }
    }
}
