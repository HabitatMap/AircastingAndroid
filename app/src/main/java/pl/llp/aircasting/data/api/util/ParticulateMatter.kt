package pl.llp.aircasting.data.api.util

enum class ParticulateMatter : SensorInformation {
    AIRBEAM {
        override fun getSensorName(): String = "airbeam2-pm2.5"
    },
    OPEN_AQ {
        override fun getSensorName(): String = "openaq-pm2.5"
    },
    PURPLE_AIR {
        override fun getSensorName(): String = "purpleair-pm2.5"
    };

    override fun getMeasurementType(): String = "ParticulateMatter"
    override fun getUnitSymbol() = "µg/m³"
}