package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import android.content.Context
import java.io.File

class SDCardCSVFileFactory(private val mContext: Context) {
    companion object {
        private const val DIR_NAME = "sync"
        private const val MOBILE_DIR_NAME = "mobile"
        private const val FIXED_DIR_NAME = "fixed"
    }

    enum class Header(val value: Int) {
        INDEX(0),
        UUID(1),
        DATE(2),
        TIME(3),
        LATITUDE(4),
        LONGITUDE(5),
        F(6),
        C(7),
        K(8),
        RH(9),
        PM1(10),
        PM2_5(11),
        PM10(12);

        companion object {
            fun fromInt(value: Int) = values().first { it.value == value }
        }
    }

    fun getMobileDirectory(): File? {
        return getDirectory(MOBILE_DIR_NAME)
    }

    fun getFixedDirectory(): File? {
        return getDirectory(FIXED_DIR_NAME)
    }

    fun getDirectory(stepType: SDCardReader.StepType): File? {
        // add sessions files
        return when (stepType) {
            SDCardReader.StepType.MOBILE -> getMobileDirectory()
            SDCardReader.StepType.FIXED_WIFI -> getFixedDirectory()
            SDCardReader.StepType.FIXED_CELLULAR -> getFixedDirectory()
        }
    }

    private fun getDirectory(subDirectory: String): File? = mContext.getExternalFilesDir(
        "$DIR_NAME/$subDirectory"
    )
}
