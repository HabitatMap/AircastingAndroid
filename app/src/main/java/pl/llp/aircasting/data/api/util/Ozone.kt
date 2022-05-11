package pl.llp.aircasting.data.api.util

enum class Ozone : SensorInformation {
    OPEN_AQ {
        override fun getSensorName(): String = "openaq-o3"
    };

    override fun getMeasurementType(): String = "Ozone"
    override fun getUnitSymbol() = "ppb"
}