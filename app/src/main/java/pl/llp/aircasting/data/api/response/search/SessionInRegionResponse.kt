package pl.llp.aircasting.data.api.response.search

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SessionInRegionResponse(
    @SerializedName("end_time_local")
    val endTimeLocal: String,
    @SerializedName("id")
    val id: Long,
    @SerializedName("is_indoor")
    val isIndoor: Boolean,
    @SerializedName("last_hour_average")
    val lastHourAverage: Double,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("start_time_local")
    val startTimeLocal: String,
    @SerializedName("streams")
    val streams: Streams,
    @SerializedName("title")
    val title: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("uuid")
    val uuid: String
)