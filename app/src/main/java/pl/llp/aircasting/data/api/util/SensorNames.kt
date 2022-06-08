package pl.llp.aircasting.data.api.util

enum class SensorNames : SensorName {
    AIRBEAM {
        override fun getSensorName(): String = "AirBeam"
    },
    OPEN_AQ {
        override fun getSensorName(): String = "OpenAQ"
    },
    PURPLE_AIR {
        override fun getSensorName(): String = "PurpleAir"
    },
    OZONE {
        override fun getSensorName(): String = "Ozone"
    };
}