package io.lunarlogic.aircasting.sensor

import android.content.Context
import io.lunarlogic.aircasting.sensor.airbeam3.sync.SDCardCSVFileFactory
import io.lunarlogic.aircasting.sensor.airbeam3.sync.SDCardReader.Step
import java.io.FileReader

class SDCardCSVFileChecker(mContext: Context) {
    private val EXPECTED_FIELDS_COUNT = 13

    private var mSteps: List<Step> = listOf()
    private var mCounts = ArrayList<HashMap<String, Int>>()

    private val mCSVFileFactory = SDCardCSVFileFactory(mContext)

    // TODO: return false only if > 20% is wrong
    fun run(steps: List<Step>): Boolean {
        val file = mCSVFileFactory.get()
        val reader = FileReader(file)
        val lines = reader.readLines()
        var stepIndex = -1

        mSteps = steps
        mCounts = ArrayList(steps.map { hashMapOf<String, Int>() })

        lines.forEachIndexed { i, line ->
            val splittedLine = line.split(",")

            if (!line.isBlank() && splittedLine.count() != EXPECTED_FIELDS_COUNT) {
                return false
            }

            val index = splittedLine.firstOrNull() ?: return false
            // TODO: think of a better way?
            if (index == "1") {
                stepIndex += 1
            }

            updateCounts(stepIndex, index)
        }

        if (!checkCounts()) {
            return false
        }

        return true
    }

    private fun updateCounts(stepIndex: Int, index: String) {
        var count = 0

        if (mCounts[stepIndex].containsKey(index)) {
            count = mCounts[stepIndex][index] ?: 0
        }

        count += 1
        mCounts[stepIndex][index] = count
    }


    private fun checkCounts(): Boolean {
        return mSteps.filterIndexed { stepIndex, step -> !checkPartialCounts(stepIndex, step) }.isEmpty()
    }

    private fun checkPartialCounts(stepIndex: Int, step: Step): Boolean {
        for (i in (1..step.measurementsCount)) {
            val index = i.toString()
            if (!mCounts[stepIndex].containsKey(index)) {
                return false
            }
            val count = mCounts[stepIndex][index]
            if (count != 1) {
                return false
            }
        }

        return true
    }
}
