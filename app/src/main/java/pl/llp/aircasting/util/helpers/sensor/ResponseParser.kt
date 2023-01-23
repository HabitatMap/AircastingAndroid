package pl.llp.aircasting.util.helpers.sensor

import android.util.Log
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.util.events.NewMeasurementEvent
import pl.llp.aircasting.util.exceptions.AirBeamResponseParsingError
import pl.llp.aircasting.util.exceptions.ErrorHandler


class ResponseParser(private val errorHandler: ErrorHandler) {
    /**
     * This has to match what Arduino produces
     * Value;Sensor package name;Sensor name;Type of measurement;Short type of measurement;Unit name;Unit symbol/abbreviation;T1;T2;T3;T4;T5
     */
    internal enum class Fields {
        MEASUREMENT_VALUE,

        SENSOR_PACKAGE_NAME,
        SENSOR_NAME,

        MEASUREMENT_TYPE,
        MEASUREMENT_SHORT_TYPE,
        MEASUREMENT_UNIT,
        MEASUREMENT_SYMBOL,

        THRESHOLD_VERY_LOW,
        THRESHOLD_LOW,
        THRESHOLD_MEDIUM,
        THRESHOLD_HIGH,
        THRESHOLD_VERY_HIGH
    }

    fun parse(line: String): NewMeasurementEvent? {
        val parts = line.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (parts.size < Fields.values().size) {
            val error = AirBeamResponseParsingError(line)
            Log.e(TAG, "${error.message}\n${error.stackTraceToString()}")
            return null
        }

        val packageName = parts[Fields.SENSOR_PACKAGE_NAME.ordinal]
        val sensorName = parts[Fields.SENSOR_NAME.ordinal]
        val measurementType = parts[Fields.MEASUREMENT_TYPE.ordinal]
        val shortMeasurnameType = parts[Fields.MEASUREMENT_SHORT_TYPE.ordinal]
        val unit = parts[Fields.MEASUREMENT_UNIT.ordinal]
        val symbol = parts[Fields.MEASUREMENT_SYMBOL.ordinal]

        var veryLow: Int?
        var low: Int?
        var mid: Int?
        var high: Int?
        var veryHigh: Int?
        var measuredValue: Double?
        try {
            veryLow = Integer.parseInt(parts[Fields.THRESHOLD_VERY_LOW.ordinal])
            low = Integer.parseInt(parts[Fields.THRESHOLD_LOW.ordinal])
            mid = Integer.parseInt(parts[Fields.THRESHOLD_MEDIUM.ordinal])
            high = Integer.parseInt(parts[Fields.THRESHOLD_HIGH.ordinal])
            veryHigh = Integer.parseInt(parts[Fields.THRESHOLD_VERY_HIGH.ordinal])

            measuredValue = java.lang.Double.parseDouble(parts[Fields.MEASUREMENT_VALUE.ordinal])
        } catch (e: NumberFormatException) {
            AirBeamResponseParsingError(line, e).printStackTrace()
            return null
        }

        return NewMeasurementEvent(
            packageName,
            sensorName,
            measurementType,
            shortMeasurnameType,
            unit,
            symbol,
            veryLow,
            low,
            mid,
            high,
            veryHigh,
            measuredValue
        )
    }
}
