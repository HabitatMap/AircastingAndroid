package pl.llp.aircasting.ui.view.screens.dashboard.charts

import com.github.mikephil.charting.data.Entry
import pl.llp.aircasting.data.api.Constants
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import java.util.*

open class ExternalChartAveragesCreator : ChartAveragesCreator() {
    override fun getFixedEntries(
        stream: MeasurementStream,
        setStartEndTimeCallback: ((startTime: Date, endTime: Date) -> Unit)?
    ): MutableList<Entry> {
        if (stream.measurements.isEmpty()) return mutableListOf()

        val calendar = Calendar.getInstance()
        setAllowedTimeLimitToCalendar(stream, calendar)

        val measurements = getMeasurementsAfterAllowedTimeLimit(stream, calendar)
        var numberOfDots = MIN_X_VALUE
        val entries: MutableList<Entry> = mutableListOf()

        if (measurements.isEmpty()) return entries

        val periodData = groupMeasurementsByHours(measurements)
        if (periodData.isNotEmpty()) {
            // From time to time we still get 10 entries, so this is another check
            val lastNineHoursMeasurementGroups = periodData.entries.toList().takeLast(9)

            val firstEntryDate = getStartDateOfEntries(lastNineHoursMeasurementGroups)
            val lastEntryDate = getEndDateForEntries(lastNineHoursMeasurementGroups)

            for (dataChunk in lastNineHoursMeasurementGroups) {
                if (numberOfDots > MAX_AVERAGES_AMOUNT) return entries

                val currentEntryDate = dataChunk.key

                val yValue = getAverage(dataChunk.value).toFloat()
                val xValue = getXvalueBasedOnTimeDifference(currentEntryDate, firstEntryDate)
                entries.add(
                    Entry(
                        xValue,
                        yValue
                    )
                )
                numberOfDots++
            }

            if (setStartEndTimeCallback != null) {
                setStartEndTimeCallback(
                    modifyHours(firstEntryDate),
                    modifyHours(lastEntryDate)
                )
            }
        }
        return entries
    }

    protected open fun getEndDateForEntries(lastNineHoursMeasurementGroups: List<Map.Entry<Date, List<Measurement>>>) =
        lastNineHoursMeasurementGroups.last().key

    protected open fun getStartDateOfEntries(lastNineHoursMeasurementGroups: List<Map.Entry<Date, List<Measurement>>>) =
        lastNineHoursMeasurementGroups.first().key

    protected open fun modifyHours(date: Date, hours: Int = 1): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.HOUR_OF_DAY, hours)
        return calendar.time
    }

    private fun getXvalueBasedOnTimeDifference(
        currentEntryTime: Date,
        firstEntryTime: Date
    ): Float {
        return ((currentEntryTime.time - firstEntryTime.time) / Constants.MILLIS_IN_HOUR).toFloat()
    }

    private fun getMeasurementsAfterAllowedTimeLimit(
        stream: MeasurementStream,
        boundary: Calendar
    ) = stream.measurements.sortedBy { it.time }.filter { it.time > boundary.time }

    private fun setAllowedTimeLimitToCalendar(
        stream: MeasurementStream,
        calendar: Calendar
    ) {
        val latestTime = stream.measurements.maxOf { it.time }
        calendar.time = latestTime
        calendar.add(Calendar.HOUR_OF_DAY, -9)
    }

    private fun groupMeasurementsByHours(
        measurements: List<Measurement>,
    ) = measurements.groupBy {
        DateUtils.truncate(it.time, Calendar.HOUR_OF_DAY)
    }
}