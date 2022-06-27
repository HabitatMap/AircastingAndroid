package pl.llp.aircasting.data.api.util

import pl.llp.aircasting.data.api.Constants

enum class Ozone : SensorInformation {
    OPEN_AQ {
        override fun getSensorName(): String = Constants.openAQsensorNameOzone
    };

    override fun getMeasurementType(): String = Constants.measurementTypeOzone
    override fun getUnitSymbol() = Constants.partsPerBillion
}