package pl.llp.aircasting.sensor.airbeam3.sync

import pl.llp.aircasting.sensor.airbeam3.sync.SDCardReader.Step
import java.io.File
import java.io.FileReader

class SDCardCSVFileChecker(
    private val mCSVFileFactory: SDCardCSVFileFactory
) {
    companion object {
        val EXPECTED_FIELDS_COUNT = 13
    }

    private val ACCEPTANCE_THRESHOLD = 0.8
    private val ACCEPTANCE_THRESHOLD_LOW_MTU = 0.2

    class Stats(val allCount: Int, val corruptedCount: Int)

    fun run(steps: List<Step>): Boolean {
        if (!checkMobile(steps)) return false

        return checkFixed(steps)
    }

    private fun checkMobile(steps: List<Step>): Boolean {
        val mobileStep = getStep(steps, SDCardReader.StepType.MOBILE) ?: return false
        val expectedCount = mobileStep.measurementsCount

        val file = mCSVFileFactory.getMobileFile()
        return check(file, expectedCount)
    }

    private fun checkFixed(steps: List<Step>): Boolean {
        val wifiStep = getStep(steps, SDCardReader.StepType.FIXED_WIFI) ?: return false
        val cellularStep = getStep(steps, SDCardReader.StepType.FIXED_CELLULAR) ?: return false
        val expectedCount = wifiStep.measurementsCount + cellularStep.measurementsCount

        val file = mCSVFileFactory.getFixedFile()
        return check(file, expectedCount)
    }

    private fun check(file: File, expectedCount: Int): Boolean {
        val stats = calculateStats(file)
        return validateAcceptedCorruption(stats, expectedCount)
    }

    private fun calculateStats(file: File): Stats {
        val reader = FileReader(file)
        var allCount = 0
        var corruptedCount = 0

        reader.forEachLine { line ->
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
        val countThreshold = expectedCount * getAcceptanceThreshold()
        val corruptionThreshold = expectedCount * (1 - getAcceptanceThreshold())

        // checks if downloaded file has at least 80% of expected lines
        // and if there is at most 20% of corrupted lines
        return stats.allCount >= countThreshold && stats.corruptedCount < corruptionThreshold
    }

    private fun getAcceptanceThreshold(): Double {
        return if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N) {
            ACCEPTANCE_THRESHOLD_LOW_MTU
        } else {
            ACCEPTANCE_THRESHOLD
        }
    }

    private fun getStep(steps: List<Step>, stepType: SDCardReader.StepType): Step? {
        return steps.find { step -> step.type == stepType }
    }
}
