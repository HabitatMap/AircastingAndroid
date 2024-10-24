package pl.llp.aircasting.data.api.util

enum class ParticulateMatter : SensorInformation {
    AIRBEAM {
        override fun getSensorName(): String = StringConstants.airbeamSensorName
    },
    GOVERNMENT {
        override fun getSensorName(): String = StringConstants.governmentSensorNamePM
    };

    override fun getMeasurementType(): String = StringConstants.measurementTypePM
    override fun getUnitSymbol() = StringConstants.nanoGrammsPerCubicMeter
}