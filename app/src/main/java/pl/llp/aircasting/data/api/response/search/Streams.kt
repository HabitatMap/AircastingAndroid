package pl.llp.aircasting.data.api.response.search

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import pl.llp.aircasting.data.api.Constants

@Keep
data class Streams(
    @SerializedName(
        "Sensor",
        alternate = [
            Constants.responseAirbeam2SensorName,
            Constants.responseAirbeam3SensorName,
            Constants.responsePurpleAirSensorName,
            Constants.responseOpenAQSensorNamePM,
            Constants.responseOpenAQSensorNameOzone
        ]
    )
    val sensor: Sensor,
)