package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import java.io.File
import java.io.FileReader

class SDCardCSVFileChecker(
    private val mCSVFileFactory: SDCardCSVFileFactory
) {
    private val EXPECTED_FIELDS_COUNT = 13
    private val ACCEPTANCE_THRESHOLD = 0.8

    class Stats(val allCount: Int, val corruptedCount: Int)

    fun checkFiles(steps: List<SDCardReader.Step>): Boolean {
        steps.forEach { step ->
            val dir = mCSVFileFactory.getDirectory(step.type)
            val dirFiles = dir?.listFiles()
            dirFiles?.forEach {
                val expectedCount = step.measurementsCount

            }
        }

        if (!checkMobile(steps)) return false

        return checkFixed(steps)
    }

    private fun checkMobile(steps: List<SDCardReader.Step>): Boolean {
        val mobileStep = getStep(steps, SDCardReader.StepType.MOBILE) ?: return false
        val expectedCount = mobileStep.measurementsCount

        val file = mCSVFileFactory.getMobileDirectory()
        return check(file, expectedCount)
    }

    private fun checkFixed(steps: List<SDCardReader.Step>): Boolean {
        val wifiStep = getStep(steps, SDCardReader.StepType.FIXED_WIFI) ?: return false
        val cellularStep = getStep(steps, SDCardReader.StepType.FIXED_CELLULAR) ?: return false
        val expectedCount = wifiStep.measurementsCount + cellularStep.measurementsCount

        val file = mCSVFileFactory.getFixedDirectory()
        return check(file, expectedCount)
    }

    private fun check(file: File?, expectedCount: Int): Boolean {
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

    private fun getStep(steps: List<SDCardReader.Step>, stepType: SDCardReader.StepType): SDCardReader.Step? {
        return steps.find { step -> step.type == stepType }
    }
}
