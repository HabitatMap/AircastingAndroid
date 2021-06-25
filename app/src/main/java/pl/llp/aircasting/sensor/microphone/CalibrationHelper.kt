package pl.llp.aircasting.sensor.microphone

import pl.llp.aircasting.lib.Settings

class CalibrationHelper(private val mSettings: Settings) {
    val OFFSET_POINT = 60.0

    fun calibrate(value: Double): Double {
        val calibration = mSettings.getCalibrationValue()

        return calibrate(value, calibration)
    }

    fun calibrate(value: Double, calibration: Int): Double {
        val low = -(calibration - OFFSET_POINT)

        return if (low == 0.0) 0.0 else project(value, low, 0.0, OFFSET_POINT, calibration.toDouble())
    }

    private fun project(
        value: Double,
        fromBeg: Double,
        fromEnd: Double,
        toBeg: Double,
        toEnd: Double
    ): Double {
        val temp = (value - fromBeg) / (fromEnd - fromBeg)
        return toBeg + temp * (toEnd - toBeg)
    }
}
