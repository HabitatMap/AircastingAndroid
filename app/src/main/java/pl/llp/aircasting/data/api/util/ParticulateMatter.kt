package pl.llp.aircasting.data.api.util

enum class ParticulateMatter : SensorInformation {
    AIRBEAM2 {
        override fun getSensorName(): String = StringConstants.airbeam2sensorName
    },
    AIRBEAM3 {
        override fun getSensorName(): String = StringConstants.airbeam3sensorName
    },
    OPEN_AQ {
        override fun getSensorName(): String = StringConstants.openAQsensorNamePM
    },
    PURPLE_AIR {
        override fun getSensorName(): String = StringConstants.purpleAirSensorName
    };

    override fun getMeasurementType(): String = StringConstants.measurementTypePM
    override fun getUnitSymbol() = StringConstants.nanoGrammsPerCubicMeter
}