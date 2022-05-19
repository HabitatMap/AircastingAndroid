package pl.llp.aircasting.data.api.responses.search

import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class Streams(
    @SerializedName(
        "Sensor",
        alternate = ["AirBeam2-PM2.5", "PurpleAir-PM2.5", "OpenAQ-PM2.5", "OpenAQ-O3"]
    )
    val sensor: Sensor,
)