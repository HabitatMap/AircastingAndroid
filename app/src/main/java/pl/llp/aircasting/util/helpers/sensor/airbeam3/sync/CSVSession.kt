package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.DateConverter
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

class CSVSession(
    val uuid: String?,
    val averagingFrequency: Int = 1,
    val streams: MutableMap<Int, ArrayList<CSVMeasurement>> = ConcurrentHashMap(),
) {
    companion object {
        const val DEFAULT_NAME = "Imported from SD card"
        const val DATE_FORMAT = "MM/dd/yyyy HH:mm:ss"

        private const val AB_DELIMITER = ","

        fun uuidFrom(line: String?): String? {
            line ?: return null

            return lineParameters(line)[LineParameter.UUID.position]
        }

        fun timestampFrom(line: String?): Date? {
            line ?: return null

            val lineParameters = lineParameters(line)
            val dateString =
                "${lineParameters[LineParameter.Date.position]} ${lineParameters[LineParameter.Time.position]}"
            return DateConverter.fromString(
                dateString,
                dateFormat = DATE_FORMAT
            )
        }

        fun lineParameters(line: String): List<String> = line.split(AB_DELIMITER)

        private const val PM_MEASUREMENT_TYPE = "Particulate Matter"
        private const val PM_MEASUREMENT_SHORT_TYPE = "PM"
        private const val PM_UNIT_NAME = "microgram per cubic meter"
        private const val PM_UNIT_SYMBOL = "µg/m³"

        val SUPPORTED_STREAMS = hashMapOf(
            LineParameter.F to CSVMeasurementStream(
                "${CSVMeasurementStream.DEVICE_NAME}-F",
                "Temperature",
                "F",
                "fahrenheit",
                "F",
                15,
                45,
                75,
                105,
                135
            ),
            LineParameter.RH to CSVMeasurementStream(
                "${CSVMeasurementStream.DEVICE_NAME}-RH",
                "Humidity",
                "RH",
                "percent",
                "%",
                0,
                25,
                50,
                75,
                100
            ),
            LineParameter.PM1 to CSVMeasurementStream(
                "${CSVMeasurementStream.DEVICE_NAME}-PM1",
                PM_MEASUREMENT_TYPE,
                PM_MEASUREMENT_SHORT_TYPE,
                PM_UNIT_NAME,
                PM_UNIT_SYMBOL,
                0,
                12,
                35,
                55,
                150
            ),
            LineParameter.PM2_5 to CSVMeasurementStream(
                "${CSVMeasurementStream.DEVICE_NAME}-PM2.5",
                PM_MEASUREMENT_TYPE,
                PM_MEASUREMENT_SHORT_TYPE,
                PM_UNIT_NAME,
                PM_UNIT_SYMBOL,
                0,
                12,
                35,
                55,
                150
            ),
            LineParameter.PM10 to CSVMeasurementStream(
                "${CSVMeasurementStream.DEVICE_NAME}-PM10",
                PM_MEASUREMENT_TYPE,
                PM_MEASUREMENT_SHORT_TYPE,
                PM_UNIT_NAME,
                PM_UNIT_SYMBOL,
                0,
                20,
                50,
                100,
                200
            )
        )

        fun fromHeader(streamLineParameter: LineParameter): CSVMeasurementStream? {
            return SUPPORTED_STREAMS[streamLineParameter]
        }
    }

    sealed class LineParameter(val position: Int) {
        object Index : LineParameter(0)
        object UUID : LineParameter(1)
        object Date : LineParameter(2)
        object Time : LineParameter(3)
        object Latitude : LineParameter(4)
        object Longitude : LineParameter(5)
        object F : LineParameter(6)
        object C : LineParameter(7)
        object K : LineParameter(8)
        object RH : LineParameter(9)
        object PM1 : LineParameter(10)
        object PM2_5 : LineParameter(11)
        object PM10 : LineParameter(12)

        companion object {
            fun fromInt(position: Int) = when (position) {
                Index.position -> Index
                UUID.position -> UUID
                Date.position -> Date
                Time.position -> Time
                Latitude.position -> Latitude
                Longitude.position -> Longitude
                F.position -> F
                C.position -> C
                K.position -> K
                RH.position -> RH
                PM1.position -> PM1
                PM2_5.position -> PM2_5
                PM10.position -> PM10
                else -> null
            }
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

    fun addMeasurements(line: String, measurementTime: Date? = null) {
        val lineParameters = lineParameters(line)
        val latitude = getValueFor(lineParameters, LineParameter.Latitude)
        val longitude = getValueFor(lineParameters, LineParameter.Longitude)
        val dateString =
            "${lineParameters[LineParameter.Date.position]} ${lineParameters[LineParameter.Time.position]}"
        val time = measurementTime ?: DateConverter.fromString(
            dateString,
            dateFormat = DATE_FORMAT
        )

        val supportedStreamHeaders = SUPPORTED_STREAMS.keys
        supportedStreamHeaders.forEach { streamHeader ->
            if (!streams.containsKey(streamHeader.position)) {
                streams[streamHeader.position] = ArrayList()
            }

            val measurement =
                buildMeasurement(latitude, longitude, time, lineParameters, streamHeader)
            measurement?.let { streams[streamHeader.position]?.add(measurement) }
        }
    }

    fun addMeasurement(
        measurement: CSVMeasurement,
        streamLineParameter: LineParameter
    ) {
        if (!streams.containsKey(streamLineParameter.position)) {
            streams[streamLineParameter.position] = ArrayList()
        }
        streams[streamLineParameter.position]?.add(measurement)
    }

    private fun buildMeasurement(
        latitude: Double?,
        longitude: Double?,
        time: Date?,
        line: List<String>,
        streamLineParameter: LineParameter
    ): CSVMeasurement? {
        time ?: return null

        val value = getValueFor(line, streamLineParameter) ?: return null

        return CSVMeasurement(
            value,
            latitude,
            longitude,
            time,
            averagingFrequency
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

    private fun getValueFor(line: List<String>, lineParameter: LineParameter): Double? {
        return try {
            line[lineParameter.position].toDouble()
        } catch (e: NumberFormatException) {
            null
        }
    }

    override fun toString(): String {
        return "CSVSession(uuid=$uuid, streams=$streams)"
    }
}
