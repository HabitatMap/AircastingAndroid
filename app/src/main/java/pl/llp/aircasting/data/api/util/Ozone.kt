package pl.llp.aircasting.data.api.util

enum class Ozone : SensorInformation {
    OPEN_AQ {
        override fun getSensorName(): String = StringConstants.openAQsensorNameOzone
    };

    override fun getMeasurementType(): String = StringConstants.measurementTypeOzone
    override fun getUnitSymbol() = StringConstants.partsPerBillion
}