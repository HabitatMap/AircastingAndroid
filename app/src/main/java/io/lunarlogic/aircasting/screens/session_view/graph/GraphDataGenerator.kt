package io.lunarlogic.aircasting.screens.session_view.graph

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.data.Entry
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.CalendarUtils
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.Note
import java.util.*
import kotlin.collections.ArrayList

class GraphDataGenerator(
    private val mContext: Context
) {
    private var cumulativeValue = 0.0
    private var cumulativeTime: Long = 0
    private var count = 0
    private var startTime = Date()
    private var hasNote = false

    private val DEFAULT_LIMIT = 1000

    class Result(val entries: List<Entry>, val midnightPoints: List<Float>)

    // Generate method is in fact triggered every time we add new measurement to session, what means fillFactor is different every time too as "samples.size" differs
    fun generate(samples: List<Measurement>, notes: List<Note>?, limit: Int = DEFAULT_LIMIT, visibleMeasurementsSize: Int?): Result {
        reset()

        val entries = ArrayList<Entry>()
        val midnightPoints = ArrayList<Float>()
        val visibleMeasurementsSize = visibleMeasurementsSize ?: samples.size
        // fillFactor is responsible for controlling the number of measurements we average when generating the Entries set
        // e.g. if samples.size is less then DEFAULT_LIMIT, fillFactor is more then 1- it means we draw entry for each measurement
        // if samples.size is a bit more then DEFAULT_LIMIT then the fillFactor is ~~0.6-0.9, what means that we build one entry per 2 measurements
        val fillFactor = 1.0 * limit / visibleMeasurementsSize
        var fill = 0.0
        Log.i("GRAPH_GENERATOR", visibleMeasurementsSize.toString())

        val firstMeasurement = samples.firstOrNull()
        firstMeasurement ?: return Result(entries, midnightPoints)
        startTime = firstMeasurement.time

        var lastDateDayOfMonth = CalendarUtils.dayOfMonth(startTime)

        for (measurement in samples) {
            add(measurement, notes)
            fill += fillFactor

            if (fill > 1) {
                fill = 0.0
                val date = getAverageDate()

                entries.add(buildAverageEntry(date, hasNote))

                val dateOfMonth = CalendarUtils.dayOfMonth(date)

                if (lastDateDayOfMonth != dateOfMonth) {
                    lastDateDayOfMonth = dateOfMonth
                    midnightPoints.add(convertDateToFloat(date))
                }

                reset()
            }
        }

        if (count > 0) {
            val date = getAverageDate()
            entries.add(buildAverageEntry(date))
        }

        return Result(entries, midnightPoints)
    }

    fun dateFromFloat(float: Float): Date {
        return Date(float.toLong() + startTime.time)
    }

    private fun getAverageDate(): Date {
        return Date(cumulativeTime / count)
    }

    private fun getAverageValue(): Double {
        return (cumulativeValue / count)
    }

    private fun buildAverageEntry(date: Date, hasNote: Boolean = false): Entry {
        val time = convertDateToFloat(date)
        val value = getAverageValue().toFloat()

        if (hasNote) {
            return Entry(time, value, ContextCompat.getDrawable(mContext, R.drawable.ic_note_icon))
        } else {
            return Entry(time, value)
        }
    }

    private fun convertDateToFloat(date: Date): Float {
        // we need to substract startTime because
        // otherwise we lose precision while converting Long to Float
        // and Float is needed for the MPAndroidChart library
        return (date.time - startTime.time).toFloat()
    }

    private fun add(measurement: Measurement, notes: List<Note>?) {
        cumulativeValue += measurement.value
        cumulativeTime += measurement.time.time
        count += 1
        if (hasNote != true && notes != null) {
            for (note in notes) {
                if (isSameDate(note, Date(measurement.time.time))) hasNote = true
            }
        }
    }

    private fun reset() {
        count = 0
        cumulativeTime = count.toLong()
        cumulativeValue = cumulativeTime.toDouble()
        hasNote = false
    }

    private fun isSameDate(note: Note, date: Date): Boolean {
        return note.date.month == date.month &&
                note.date.day == date.day &&
                note.date.hours == date.hours &&
                note.date.minutes == date.minutes &&
                note.date.seconds == date.seconds
    }
}
