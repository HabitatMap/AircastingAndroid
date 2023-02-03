package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardCSVFileFactory.Companion.AB_DELIMITER
import java.util.*

class CSVSession(
    val uuid: String?,
    val streams: HashMap<Int, ArrayList<CSVMeasurement>> = HashMap()
) {
    companion object {
        const val DEFAULT_NAME = "Imported from SD card"
        const val DATE_FORMAT = "MM/dd/yyyy HH:mm:ss"

        fun uuidFrom(line: String?): String? {
            line ?: return null

            return lineParameters(line)[SDCardCSVFileFactory.Header.UUID.value]
        }

        fun timestampFrom(line: String?): Date? {
            line ?: return null

            val lineParameters = lineParameters(line)
            val dateString =
                "${lineParameters[SDCardCSVFileFactory.Header.DATE.value]} ${lineParameters[SDCardCSVFileFactory.Header.TIME.value]}"
            return DateConverter.fromString(
                dateString,
                dateFormat = DATE_FORMAT
            )
        }

        private fun lineParameters(line: String): List<String> = line.split(AB_DELIMITER)

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

    fun addMeasurements(line: String) {
        val lineParameters = lineParameters(line)
        val latitude = getValueFor(lineParameters, SDCardCSVFileFactory.Header.LATITUDE)
        val longitude = getValueFor(lineParameters, SDCardCSVFileFactory.Header.LONGITUDE)
        val dateString =
            "${lineParameters[SDCardCSVFileFactory.Header.DATE.value]} ${lineParameters[SDCardCSVFileFactory.Header.TIME.value]}"
        val time = DateConverter.fromString(
            dateString,
            dateFormat = DATE_FORMAT
        )

        val supportedStreamHeaders = CSVMeasurementStream.SUPPORTED_STREAMS.keys
        supportedStreamHeaders.forEach { streamHeader ->
            if (!streams.containsKey(streamHeader.value)) {
                streams[streamHeader.value] = ArrayList()
            }

            val measurement =
                buildMeasurement(latitude, longitude, time, lineParameters, streamHeader)
            measurement?.let { streams[streamHeader.value]?.add(measurement) }
        }
    }

    private fun buildMeasurement(
        latitude: Double?,
        longitude: Double?,
        time: Date?,
        line: List<String>,
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

    fun toSession(deviceId: String): Session? {
        val startTime = startTime() ?: return null
        uuid ?: return null

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

            if (location == Session.Location.FAKE_LOCATION) {
                session.locationless = true
            }
        }

        return session
    }

    private fun getValueFor(line: List<String>, header: SDCardCSVFileFactory.Header): Double? {
        return try {
            line[header.value].toDouble()
        } catch (e: NumberFormatException) {
            null
        }
    }

    override fun toString(): String {
        return "CSVSession(uuid=$uuid, streams=$streams)"
    }
}
