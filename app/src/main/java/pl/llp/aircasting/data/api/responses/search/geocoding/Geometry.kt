package pl.llp.aircasting.data.api.responses.search.geocoding


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class Geometry(
    @SerializedName("location")
    val location: Location,
    @SerializedName("location_type")
    val locationType: String,
    @SerializedName("viewport")
    val viewport: Viewport
)