package pl.llp.aircasting.data.api.response.search


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

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
    val sensorPackageName: String,
    @SerializedName("session_id")
    val sessionId: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("start_latitude")
    val startLatitude: Double,
    @SerializedName("start_longitude")
    val startLongitude: Double,
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
    val unitName: String,
    @SerializedName("unit_symbol")
    val unitSymbol: String
)