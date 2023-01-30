package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileReader

class SDCardCSVFileChecker {
    private val EXPECTED_FIELDS_COUNT = 13
    private val ACCEPTANCE_THRESHOLD = 0.8

    class Stats(val allCount: Int, val corruptedCount: Int)

    fun checkFilesForCorruption(stepsByFilePaths: Map<SDCardReader.Step?, List<String>>) = flow {
        stepsByFilePaths.forEach { entry ->
            val file = File(entry.key)
            val numberOfMeasurementsInFile = entry.value
            val result = checkForCorruption(file, numberOfMeasurementsInFile)
            emit(file to result)
        }
    }

    private fun checkForCorruption(file: File?, expectedCount: Int): Boolean {
        val stats = calculateStats(file)
        return validateAcceptedCorruption(stats, expectedCount)
    }

    private fun calculateStats(file: File?): Stats {
        val reader = try { FileReader(file) } catch (e: Exception) { null }
        var allCount = 0
        var corruptedCount = 0

        reader?.forEachLine { line ->
            if (lineIsCorrupted(line)) {
                corruptedCount += 1
            }

            allCount += 1
        }

        return Stats(allCount, corruptedCount)
    }

    private fun lineIsCorrupted(line: String): Boolean {
        val fields = line.split(",")
        return !line.isBlank() && fields.count() != EXPECTED_FIELDS_COUNT
    }

    private fun validateAcceptedCorruption(stats: Stats, expectedCount: Int): Boolean {
        if (expectedCount == 0) return true

        val countThreshold = expectedCount * ACCEPTANCE_THRESHOLD
        val corruptionThreshold = expectedCount * (1 - ACCEPTANCE_THRESHOLD)

        // checks if downloaded file has at least 80% of expected lines
        // and if there is at most 20% of corrupted lines
        return stats.allCount >= countThreshold && stats.corruptedCount < corruptionThreshold
    }
}
