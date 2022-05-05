package pl.llp.aircasting.data.api.responses.search


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class Streams(
    @SerializedName("AirBeam2-PM2.5")
    val airBeam2PM25: AirBeam2PM25
)