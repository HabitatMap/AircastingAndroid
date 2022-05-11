package pl.llp.aircasting.data.api.util

interface SensorInformation {
    fun getSensorName(): String
    fun getUnitSymbol(): String
    fun getMeasurementType(): String
}