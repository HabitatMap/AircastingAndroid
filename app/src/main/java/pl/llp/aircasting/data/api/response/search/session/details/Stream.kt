package pl.llp.aircasting.data.api.response.search.session.details


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import pl.llp.aircasting.data.api.response.MeasurementResponse

@Keep
data class Stream(
    @SerializedName("last_measurement_value")
    val lastMeasurementValue: Double,
    @SerializedName("max_latitude")
    val maxLatitude: Double,
    @SerializedName("max_longitude")
    val maxLongitude: Double,
    @SerializedName("measurement_short_type")
    val measurementShortType: String,
    @SerializedName("measurement_type")
    val measurementType: String,
    @SerializedName("measurements")
    val measurements: List<MeasurementResponse>,
    @SerializedName("min_latitude")
    val minLatitude: Double,
    @SerializedName("min_longitude")
    val minLongitude: Double,
    @SerializedName("sensor_name")
    val sensorName: String,
    @SerializedName("sensor_unit")
    val sensorUnit: String,
    @SerializedName("stream_id")
    val streamId: Int,
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
    @SerializedName("unit_name")
    val unitName: String
)