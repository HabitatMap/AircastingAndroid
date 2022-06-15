package pl.llp.aircasting.data.api.response.search.session.details


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

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
    val streams: List<Stream>,
    @SerializedName("title")
    val title: String,
    @SerializedName("username")
    val username: String
)