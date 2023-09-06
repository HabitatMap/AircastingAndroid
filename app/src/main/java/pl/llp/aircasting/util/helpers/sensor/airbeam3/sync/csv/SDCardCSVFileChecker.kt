package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv

import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardReader
import java.io.File
import java.io.FileReader

class SDCardCSVFileChecker {
    companion object {
        private const val EXPECTED_FIELDS_COUNT = 13
        private const val ACCEPTANCE_THRESHOLD = 0.8
        fun lineIsCorrupted(line: String): Boolean {
            val fields = line.split(",")
            return line.isNotBlank() && fields.count() != EXPECTED_FIELDS_COUNT
        }
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
