package pl.llp.aircasting.data.api.responses.search.geocoding


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class Viewport(
    @SerializedName("northeast")
    val northeast: Northeast,
    @SerializedName("southwest")
    val southwest: Southwest
)