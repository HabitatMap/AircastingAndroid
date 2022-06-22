package pl.llp.aircasting.data.api.util

enum class ParticulateMatter : SensorInformation {
    AIRBEAM2 {
        override fun getSensorName(): String = "airbeam2-pm2.5"
    },
    AIRBEAM3 {
        override fun getSensorName(): String = "airbeam3-pm2.5"
    },
    OPEN_AQ {
        override fun getSensorName(): String = "openaq-pm2.5"
    },
    PURPLE_AIR {
        override fun getSensorName(): String = "purpleair-pm2.5"
    };

    override fun getMeasurementType(): String = "Particulate Matter"
    override fun getUnitSymbol() = "µg/m³"
}