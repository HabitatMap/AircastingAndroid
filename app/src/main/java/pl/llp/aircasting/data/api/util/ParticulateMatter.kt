package pl.llp.aircasting.data.api.util

import pl.llp.aircasting.data.api.Constants

enum class ParticulateMatter : SensorInformation {
    AIRBEAM2 {
        override fun getSensorName(): String = Constants.airbeam2sensorName
    },
    AIRBEAM3 {
        override fun getSensorName(): String = Constants.airbeam3sensorName
    },
    OPEN_AQ {
        override fun getSensorName(): String = Constants.openAQsensorNamePM
    },
    PURPLE_AIR {
        override fun getSensorName(): String = Constants.purpleAirSensorName
    };

    override fun getMeasurementType(): String = Constants.measurementTypePM
    override fun getUnitSymbol() = Constants.nanoGrammsPerCubicMeter
}