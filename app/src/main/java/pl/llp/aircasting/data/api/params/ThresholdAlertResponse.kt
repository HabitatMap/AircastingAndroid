package pl.llp.aircasting.data.api.params

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

data class CreateThresholdAlertBody(
    val data: CreateThresholdAlertData
)

@Keep
data class CreateThresholdAlertData(
    @SerializedName("frequency")
    val frequency: String,
    @SerializedName("sensor_name")
    val sensorName: String,
    @SerializedName("session_uuid")
    val sessionUuid: String,
    @SerializedName("threshold_value")
    val thresholdValue: String,
    @SerializedName("timezone_offset")
    val timezoneOffset: String
)

@Keep
data class ThresholdAlertResponse(
    @SerializedName("frequency")
    val frequency: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("sensor_name")
    val sensorName: String,
    @SerializedName("session_uuid")
    val sessionUuid: String,
    @SerializedName("threshold_value")
    val thresholdValue: Double,
    @SerializedName("timezone_offset")
    val timezoneOffset: Int
)

data class CreateThresholdAlertResponse(
    val id: Int
)