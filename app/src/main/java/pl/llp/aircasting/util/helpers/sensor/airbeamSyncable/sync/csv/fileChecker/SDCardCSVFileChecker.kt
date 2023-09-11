package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileChecker

import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardReader
import java.io.File
import java.io.FileReader

object SDCardCSVFileCheckerFactory {
    fun create(deviceType: DeviceItem.Type): SDCardCSVFileChecker = when(deviceType) {
        DeviceItem.Type.AIRBEAMMINI -> ABMiniSDCardCSVFileChecker()
        else -> AB3SDCardCSVFileChecker()
    }
}

abstract class SDCardCSVFileChecker {
    companion object {
        private const val ACCEPTANCE_THRESHOLD = 0.8
    }

    protected abstract val expectedFieldsCount: Int

    fun lineIsCorrupted(line: String): Boolean {
        val fields = line.split(",")
        return line.isNotBlank() && fields.count() != expectedFieldsCount
    }


    fun areFilesCorrupted(stepsByFilePaths: Map<SDCardReader.Step?, List<String>>): Boolean {
        stepsByFilePaths.forEach { stepByPath ->
            var countOfCorruptedFilesInCurrentStep = 0
            stepByPath.value.forEach { path ->
                countOfCorruptedFilesInCurrentStep += countCorruptedLines(File(path))
            }
            if (corruptionRateIsUnacceptable(
                    countOfCorruptedFilesInCurrentStep,
                    stepByPath.key?.measurementsCount
                )
            ) return true
        }
        return false
    }

    private fun countCorruptedLines(file: File?): Int {
        val reader = try {
            FileReader(file)
        } catch (e: Exception) {
            null
        }
        var corruptedCount = 0

        reader?.forEachLine { line ->
            if (lineIsCorrupted(line)) {
                corruptedCount += 1
            }
        }

        return corruptedCount
    }

    private fun corruptionRateIsUnacceptable(
        corruptedLinesCount: Int?,
        totalLinesCount: Int?
    ): Boolean {
        if (totalLinesCount == 0) return false
        corruptedLinesCount ?: return true
        totalLinesCount ?: return true

        val numberOfAcceptableCorruptedLines = totalLinesCount * (1 - ACCEPTANCE_THRESHOLD)

        return corruptedLinesCount > numberOfAcceptableCorruptedLines
    }
}
