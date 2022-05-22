package pl.llp.aircasting.data.api.responses.search.geocoding


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class Result(
    @SerializedName("address_components")
    val addressComponents: List<AddressComponent>,
    @SerializedName("formatted_address")
    val formattedAddress: String,
    @SerializedName("geometry")
    val geometry: Geometry,
    @SerializedName("place_id")
    val placeId: String,
    @SerializedName("plus_code")
    val plusCode: PlusCode,
    @SerializedName("types")
    val types: List<String>
)