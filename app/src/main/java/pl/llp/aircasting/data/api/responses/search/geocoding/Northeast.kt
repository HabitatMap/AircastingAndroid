package pl.llp.aircasting.data.api.responses.search.geocoding


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Northeast(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double
)