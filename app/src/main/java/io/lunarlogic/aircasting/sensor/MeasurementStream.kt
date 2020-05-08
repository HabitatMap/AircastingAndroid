package io.lunarlogic.aircasting.sensor

class MeasurementStream(measurement: Measurement) {
    private var mSensorPackageName: String? = null
    private var mSensorName: String? = null
    private var mMeasurementType: String? = null
    private var mMeasurementShortType: String? = null
    private var mUnitName: String? = null
    private var mUnitSymbol: String? = null
    private var mThresholdVeryLow: Int? = null
    private var mThresholdLow: Int? = null
    private var mThresholdMedium: Int? = null
    private var mThresholdHigh: Int? = null
    private var mThresholdVeryHigh: Int? = null

    private val mMeasurements = arrayListOf<Measurement>()

    init {
        mSensorPackageName = measurement.packageName
        mSensorName = measurement.sensorName
        mMeasurementType = measurement.measurementType
        mMeasurementShortType = measurement.measurementShortType
        mUnitName = measurement.unitName
        mUnitSymbol = measurement.unitSymbol
        mThresholdVeryLow = measurement.thresholdVeryLow
        mThresholdLow = measurement.thresholdLow
        mThresholdMedium = measurement.thresholdMedium
        mThresholdHigh = measurement.thresholdHigh
        mThresholdVeryHigh = measurement.thresholdVeryHigh
    }

    val sensorName: String get() = mSensorName!!
    val measurements: ArrayList<Measurement> get() = mMeasurements

    fun addMeasurement(measurement: Measurement) {
        mMeasurements.add(measurement)
    }
}