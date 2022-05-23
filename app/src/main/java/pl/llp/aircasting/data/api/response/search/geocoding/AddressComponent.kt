package pl.llp.aircasting.data.api.response.search.geocoding


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AddressComponent(
    @SerializedName("long_name")
    val longName: String,
    @SerializedName("short_name")
    val shortName: String,
    @SerializedName("types")
    val types: List<String>
)