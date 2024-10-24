package pl.llp.aircasting.data.api.util

enum class Ozone : SensorInformation {
    GOVERNMENT {
        override fun getSensorName(): String = StringConstants.governmentSensorNameOzone
    };

    override fun getMeasurementType(): String = StringConstants.measurementTypeOzone
    override fun getUnitSymbol() = StringConstants.partsPerBillion
}