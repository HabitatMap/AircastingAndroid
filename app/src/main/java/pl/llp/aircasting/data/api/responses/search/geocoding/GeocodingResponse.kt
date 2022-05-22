package pl.llp.aircasting.data.api.responses.search.geocoding


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class GeocodingResponse(
    @SerializedName("results")
    val results: List<Result>,
    @SerializedName("status")
    val status: String
)