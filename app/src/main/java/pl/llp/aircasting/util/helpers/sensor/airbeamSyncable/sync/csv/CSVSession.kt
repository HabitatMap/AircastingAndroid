package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv

import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.Session.Location.Companion.DEFAULT_LOCATION
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.helpers.location.LocationHelper
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.lineParameter.CSVLineParameterHandler
import pl.llp.aircasting.util.helpers.services.AveragingWindow
import java.util.concurrent.ConcurrentHashMap

class CSVSession(
    val uuid: String?,
    private val lineParameterHandler: CSVLineParameterHandler,
    private val averagingWindow: AveragingWindow = AveragingWindow.ZERO,

    val streams: MutableMap<CSVLineParameterHandler.ABLineParameter, ArrayList<CSVMeasurement>> = ConcurrentHashMap(),
) {
    companion object {
        const val DEFAULT_NAME = "Imported from AirBeam storage"
        const val DATE_FORMAT = "MM/dd/yyyy HH:mm:ss"
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
        val startTime = firstMeasurement()?.time ?: return null
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

        val latitude = firstMeasurement()?.latitude
            ?: LocationHelper.lastLocation()?.latitude
            ?: DEFAULT_LOCATION.latitude
        val longitude = firstMeasurement()?.longitude
            ?: LocationHelper.lastLocation()?.longitude
            ?: DEFAULT_LOCATION.longitude
        val location = Session.Location(latitude, longitude)
        session.location = location

        if (location == Session.Location.FAKE_LOCATION) {
            session.locationless = true
        }

        return session
    }

    override fun toString(): String {
        return "CSVSession(uuid=$uuid, streams=$streams)"
    }
}
