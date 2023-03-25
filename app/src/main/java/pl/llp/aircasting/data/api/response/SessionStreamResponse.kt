package pl.llp.aircasting.data.api.response

import com.google.gson.annotations.SerializedName

open class SessionStreamResponse(
    @SerializedName("sensor_name")
    val sensorName: String,
    @SerializedName("sensor_package_name")
    val sensorPackageName: String,
    @SerializedName("unit_name")
    val unitName: String,
    @SerializedName("measurement_type")
    val measurementType: String,
    @SerializedName("measurement_short_type")
    val measurementShortType: String,
    @SerializedName("unit_symbol")
    val unitSymbol: String,
    @SerializedName("threshold_very_low")
    val thresholdVeryLow: Int,
    @SerializedName("threshold_low")
    val thresholdLow: Int,
    @SerializedName("threshold_medium")
    val thresholdMedium: Int,
    @SerializedName("threshold_high")
    val thresholdHigh: Int,
    @SerializedName("threshold_very_high")
    val thresholdVeryHigh: Int,
    @SerializedName("results")
    val deleted: Boolean
)
