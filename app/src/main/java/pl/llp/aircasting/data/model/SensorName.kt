package pl.llp.aircasting.data.model

import pl.llp.aircasting.data.api.util.StringConstants

enum class SensorName(val detailedType: String) {
    F(StringConstants.F),
    PM(StringConstants.PM),
    PM1(StringConstants.PM1),
    PM2_5(StringConstants.PM2_5),
    PM10(StringConstants.PM10),
    RH(StringConstants.RH);

    companion object {
        fun fromString(detailedType: String?) =
            values().firstOrNull { it.detailedType == detailedType }
    }
}