package pl.llp.aircasting.data.api.responses.search.geocoding


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class Southwest(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double
)