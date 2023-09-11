package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv

import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.lineParameter.CSVLineParameterHandler
import pl.llp.aircasting.util.helpers.services.AveragingWindow
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

class CSVSession(
    val uuid: String?,
    private val lineParameterHandler: CSVLineParameterHandler,
    private val averagingWindow: AveragingWindow = AveragingWindow.ZERO,

    val streams: MutableMap<CSVLineParameterHandler.ABLineParameter, ArrayList<CSVMeasurement>> = ConcurrentHashMap(),
) {
    companion object {
        const val DEFAULT_NAME = "Imported from SD card"
        const val DATE_FORMAT = "MM/dd/yyyy HH:mm:ss"
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
        val supportedStreamHeaders = lineParameterHandler.supportedStreams.keys
        supportedStreamHeaders.forEach { streamHeader ->
            if (!streams.containsKey(streamHeader)) {
                streams[streamHeader] = ArrayList()
            }

            val measurement = lineParameterHandler.getCsvMeasurement(line, streamHeader, averagingWindow)
            measurement?.let { streams[streamHeader]?.add(measurement) }
        }
    }

    fun addMeasurement(
        measurement: CSVMeasurement,
        streamLineParameter: CSVLineParameterHandler.ABLineParameter
    ) {
        if (!streams.containsKey(streamLineParameter)) {
            streams[streamLineParameter] = ArrayList()
        }
        streams[streamLineParameter]?.add(measurement)
    }

    fun toSession(deviceId: String, deviceType: DeviceItem.Type): Session? {
        val startTime = startTime() ?: return null
        uuid ?: return null

        val session = Session(
            uuid,
            deviceId,
            deviceType,
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

    override fun toString(): String {
        return "CSVSession(uuid=$uuid, streams=$streams)"
    }
}
