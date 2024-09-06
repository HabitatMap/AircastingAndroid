package pl.llp.aircasting.data.api.util

enum class ParticulateMatter : SensorInformation {
    AIRBEAM2 {
        override fun getSensorName(): String = StringConstants.airbeam2sensorName
    },
    AIRBEAM3 {
        override fun getSensorName(): String = StringConstants.airbeam3sensorName
    },
    GOVERNMENT {
        override fun getSensorName(): String = StringConstants.governmentSensorNamePM
    };

    override fun getMeasurementType(): String = StringConstants.measurementTypePM
    override fun getUnitSymbol() = StringConstants.nanoGrammsPerCubicMeter
}