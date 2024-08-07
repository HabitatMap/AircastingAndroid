package pl.llp.aircasting.data.api.response.search.session.details

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import pl.llp.aircasting.data.api.response.search.Sensor

@Keep
data class SessionWithStreamsAndMeasurementsResponse(
    @SerializedName("end_time")
    val endTime: Long,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_indoor")
    val isIndoor: Boolean,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("notes")
    val notes: List<Any>,
    @SerializedName("start_time")
    val startTime: Long,
    @SerializedName("streams")
    val sensors: List<Sensor>,
    @SerializedName("title")
    val title: String,
    @SerializedName("username")
    val username: String
)