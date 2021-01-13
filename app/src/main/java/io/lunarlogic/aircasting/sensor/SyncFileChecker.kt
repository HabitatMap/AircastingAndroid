package io.lunarlogic.aircasting.sensor

import android.content.Context
import java.awt.font.NumericShaper
import java.io.File
import java.io.FileReader
import java.io.FileWriter

// NOTE: this class is temporary check if downloading from SD card works, before actual sync is implemeted
// TODO: remove this class after implementing sync
class SyncFileChecker(private val mContext: Context) {
    private val FIELDS_COUNT = 15
    private val bleCounts = HashMap<String, Int>()
    private val wifiCounts = HashMap<String, Int>()
    private val cellularCounts = HashMap<String, Int>()
    private var step = 0

    fun run(bleCount: Int, wifiCount: Int, cellularCount: Int): Boolean {
        val dir = mContext.getExternalFilesDir("sync")

        val file = File(dir, "sync.txt")
        val reader = FileReader(file)
        val lines = reader.readLines()

        lines.forEach { line ->
            val splittedLine = line.split(",")

            if (splittedLine.count() != FIELDS_COUNT) {
                return false
            }

            val index = splittedLine.firstOrNull() ?: return false
            if (index == "1") {
                step += 1
            }

            when(step) {
                1 -> updateCounts(bleCounts, index)
                2 -> updateCounts(wifiCounts, index)
                3 -> updateCounts(cellularCounts, index)
            }
        }

        if (!checkCounts(bleCounts, bleCount)) {
            return false
        }

        if (!checkCounts(wifiCounts, wifiCount)) {
            return false
        }

        if (!checkCounts(cellularCounts, cellularCount)) {
            return false
        }

        return true
    }

    private fun updateCounts(counts: HashMap<String, Int>, index: String) {
        var count = 0

        if (counts.containsKey(index)) {
            count = counts[index] ?: 0
        }

        count += 1
        counts[index] = count
    }

    private fun checkCounts(counts: HashMap<String, Int>, count: Int): Boolean {
        for (i in (1..count)) {
            val index = i.toString()
            if (!counts.containsKey(index)) {
                return false
            }
            val count = counts[index]
            if (count != 1) {
                return false
            }
        }

        return true
    }
}
