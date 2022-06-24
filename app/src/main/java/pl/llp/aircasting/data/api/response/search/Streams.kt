package pl.llp.aircasting.data.api.response.search

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import pl.llp.aircasting.data.api.util.StringConstants

@Keep
data class Streams(
    @SerializedName(
        "Sensor",
        alternate = [
            StringConstants.responseAirbeam2SensorName,
            StringConstants.responseAirbeam3SensorName,
            StringConstants.responsePurpleAirSensorName,
            StringConstants.responseOpenAQSensorNamePM,
            StringConstants.responseOpenAQSensorNameOzone
        ]
    )
    val sensor: Sensor,
)