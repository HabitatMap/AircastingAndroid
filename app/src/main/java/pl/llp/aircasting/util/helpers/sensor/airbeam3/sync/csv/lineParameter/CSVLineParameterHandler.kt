package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.lineParameter

import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.CSVMeasurement
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.CSVMeasurementStream
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.CSVSession
import pl.llp.aircasting.util.helpers.services.AveragingWindow
import java.util.Date


object CSVLineParameterHandlerFactory {
    fun create(deviceType: DeviceItem.Type): CSVLineParameterHandler = when (deviceType) {
        DeviceItem.Type.AIRBEAMMINI -> ABMiniCSVLineParameterHandler()
        else -> AB3CSVLineParameterHandler()
    }
}

abstract class CSVLineParameterHandler {
    abstract val timeParameter: ABLineParameter
    abstract val dateParameter: ABLineParameter
    abstract val uuidParameter: ABLineParameter

    abstract val supportedStreams: Map<ABLineParameter, CSVMeasurementStream>
    fun uuidFrom(line: String?): String? {
        line ?: return null

        return lineParameters(line)[uuidParameter.position]
    }

    fun timestampFrom(line: String?): Date? {
        line ?: return null

        val lineParameters = lineParameters(line)
        val dateString =
            "${lineParameters[dateParameter.position]} ${lineParameters[timeParameter.position]}"
        return DateConverter.fromString(
            dateString,
            dateFormat = CSVSession.DATE_FORMAT
        )
    }

    fun csvStreamByLineParameter(streamLineParameter: ABLineParameter): CSVMeasurementStream? {
        return supportedStreams[streamLineParameter]
    }

    open fun getCsvMeasurement(
        line: String,
        currentStreamLineParameter: ABLineParameter,
        finalAveragingWindow: AveragingWindow
    ): CSVMeasurement? {
        val params = lineParameters(line)
        val value = getValueFor(params, currentStreamLineParameter)
            ?: return null
        val time = timestampFrom(line) ?: return null

        return CSVMeasurement(value, null, null, time, finalAveragingWindow.value)
    }


    protected fun getValueFor(line: List<String>, lineParameter: ABLineParameter): Double? {
        return try {
            line[lineParameter.position].toDouble()
        } catch (e: NumberFormatException) {
            null
        }
    }


    sealed class ABLineParameter(open val position: Int)

    companion object {
        private const val AB_DELIMITER = ","
        const val PM_MEASUREMENT_TYPE = "Particulate Matter"
        const val PM_MEASUREMENT_SHORT_TYPE = "PM"
        const val PM_UNIT_NAME = "microgram per cubic meter"
        const val PM_UNIT_SYMBOL = "µg/m³"

        fun lineParameters(line: String): List<String> = line.split(AB_DELIMITER)
    }
}
