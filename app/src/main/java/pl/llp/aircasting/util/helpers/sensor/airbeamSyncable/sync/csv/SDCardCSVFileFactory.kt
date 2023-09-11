package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv

import android.content.Context
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardReader
import java.io.File

class SDCardCSVFileFactory(private val mContext: Context) {
    companion object {
        private const val DIR_NAME = "sync"
        private const val MOBILE_DIR_NAME = "mobile"
        private const val FIXED_DIR_NAME = "fixed"
    }

    fun getMobileDirectory(): File? {
        return getDirectory(MOBILE_DIR_NAME)
    }

    fun getFixedDirectory(): File? {
        return getDirectory(FIXED_DIR_NAME)
    }

    fun getDirectory(stepType: SDCardReader.StepType?): File? {
        // add sessions files
        return when (stepType) {
            SDCardReader.StepType.MOBILE -> getMobileDirectory()
            SDCardReader.StepType.FIXED_WIFI -> getFixedDirectory()
            SDCardReader.StepType.FIXED_CELLULAR -> getFixedDirectory()
            else -> null
        }
    }

    private fun getDirectory(subDirectory: String): File? = mContext.getExternalFilesDir(
        "$DIR_NAME/$subDirectory"
    )
}
