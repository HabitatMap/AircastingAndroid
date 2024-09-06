package pl.llp.aircasting.data.api.util

enum class NitrogenDioxide : SensorInformation {
    GOVERNMENT {
        override fun getSensorName(): String = StringConstants.governmentSensorNameNitrogenDioxide
    };

    override fun getMeasurementType(): String = StringConstants.measurementTypeNitrogenDioxide
    override fun getUnitSymbol() = StringConstants.partsPerBillion
}