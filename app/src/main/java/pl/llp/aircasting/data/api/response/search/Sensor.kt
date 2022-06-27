package pl.llp.aircasting.data.api.response.search

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import pl.llp.aircasting.data.api.response.MeasurementOfStreamResponse

@Keep
data class Sensor(
    @SerializedName("id")
    val id: Int,
    @SerializedName("measurement_short_type")
    val measurementShortType: String,
    @SerializedName("measurement_type")
    val measurementType: String,
    @SerializedName("sensor_name")
    val sensorName: String,
    @SerializedName("sensor_package_name")
    val sensorPackageName: String?,
    @SerializedName("session_id")
    val sessionId: Int,
    @SerializedName("threshold_high")
    val thresholdHigh: Int,
    @SerializedName("threshold_low")
    val thresholdLow: Int,
    @SerializedName("threshold_medium")
    val thresholdMedium: Int,
    @SerializedName("threshold_very_high")
    val thresholdVeryHigh: Int,
    @SerializedName("threshold_very_low")
    val thresholdVeryLow: Int,
    @SerializedName("last_measurement_value")
    val last_measurement_value: Double,
    @SerializedName("unit_name")
    val unitName: String,
    @SerializedName("unit_symbol", alternate = ["sensor_unit"])
    val unitSymbol: String,
    @SerializedName("measurements")
    val measurements: List<MeasurementOfStreamResponse>?,
)