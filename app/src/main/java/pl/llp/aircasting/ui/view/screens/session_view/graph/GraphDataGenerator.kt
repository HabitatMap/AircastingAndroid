package pl.llp.aircasting.ui.view.screens.session_view.graph

import android.content.Context
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.data.Entry
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.util.CalendarUtils
import pl.llp.aircasting.util.helpers.services.AveragingService
import java.util.*

class GraphDataGenerator(
    private val mContext: Context
) {
    private var cumulativeValue = 0.0
    private var cumulativeTime: Long = 0
    private var count = 0
    private var startTime = Date()
    private var hasNote = false
    private var averagingGeneratorFrequency = 0

    private val DEFAULT_LIMIT = 1000

    class Result(val entries: List<Entry>, val midnightPoints: List<Float>, val noteRanges: MutableList<ClosedRange<Long>>)

    // Generate method is in fact triggered every time we add new measurement to session, what means fillFactor is different every time too as "samples.size" differs
    fun generate(samples: List<Measurement>, notes: List<Note>?, limit: Int = DEFAULT_LIMIT, visibleMeasurementsSize: Int = samples.size, averagingFrequency: Int = 1): Result {
        reset()

        val entries = LinkedList<Entry>()
        val midnightPoints = LinkedList<Float>()
        val noteRanges = mutableListOf<ClosedRange<Long>>()
        averagingGeneratorFrequency = averagingFrequency
        // fillFactor is responsible for controlling the number of measurements we average when generating the Entries set
        // e.g. if samples.size is less then DEFAULT_LIMIT, fillFactor is more then 1- it means we draw entry for each measurement
        // if samples.size is a bit more then DEFAULT_LIMIT then the fillFactor is ~~0.6-0.9, what means that we build one entry per 2 measurements
        val fillFactor = 1.0 * limit / visibleMeasurementsSize
        var fill = 0.0

        val firstMeasurement = samples.firstOrNull()
        firstMeasurement ?: return Result(entries, midnightPoints, noteRanges)
        startTime = firstMeasurement.time

        var lastDateDayOfMonth = CalendarUtils.dayOfMonth(startTime)

        for (measurement in samples) {
            add(measurement, notes)
            fill += fillFactor

            // We use below if to decrease number of entries that are displayed on the graph
            // At the moment we exceed 'limit' (maximum number of entries displayed on the graph) we start to show less points on graph the we have measurements
            // In order to show the graph properly even though we can't display all measurements on the graph we average measurements 'adjacent' to each other depending on fillFactor
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

        entries.sortBy { it.x }

        val range = (entries.last().x - entries.first().x).div(40) // I assume clickable range as about 5% of screen
        for (entry in entries) {
            if (entry.icon != null) {
                noteRanges.add((entry.x.toLong() - range.toLong())..(entry.x.toLong() + range.toLong()))
            }
        }
        return Result(entries, midnightPoints, noteRanges)
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
        return if (hasNote) {
            Entry(time, value, ContextCompat.getDrawable(mContext, R.drawable.ic_note_icon))
        } else {
            Entry(time, value)
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

        val measurementDate = Date(measurement.time.time)
        if (hasNote != true && notes != null) {
            for (note in notes) {
                when (averagingGeneratorFrequency) {
                    AveragingService.DEFAULT_FREQUENCY -> if (isSameDate(note, measurementDate)) hasNote = true
                    AveragingService.FIRST_THRESHOLD_FREQUENCY -> if (isSameDateAveraging(note, measurementDate, AveragingService.FIRST_THRESHOLD_FREQUENCY) && measurement.averagingFrequency == AveragingService.FIRST_THRESHOLD_FREQUENCY) hasNote = true
                    AveragingService.SECOND_THRESHOLD_FREQUENCY -> if (isSameDateAveraging(note, measurementDate, AveragingService.SECOND_THRESHOLD_FREQUENCY) && measurement.averagingFrequency == AveragingService.SECOND_THRESHOLD_FREQUENCY) hasNote = true
                }
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
        return note.date == date
    }

    private fun isSameDateAveraging(note: Note, date: Date, averagingFrequency: Int): Boolean {
        val dateBefore = Date(date.time + averagingFrequency * 1000/2L) // multiplication by 1000 to have correct number of milliseconds and division by 2 to keep correct date interval
        val dateAfter = Date(date.time - averagingFrequency * 1000/2L)
        return note.date.after(dateAfter) && note.date.before(dateBefore)
    }

}